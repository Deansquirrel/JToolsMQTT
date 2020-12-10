package com.yuansong.tools.mqtt;

import java.text.MessageFormat;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yuansong.tools.common.ExceptionTool;
import com.yuansong.tools.common.IRunUntillSuccess;
import com.yuansong.tools.mqtt.config.IMqttToolConfig;

@Component
public class MqttToolClient {
	
	private static final Logger logger = LoggerFactory.getLogger(MqttToolClient.class);

	private MqttClient client = null;;
	
	@Autowired
	private IMqttToolConfig config;
	
	@Autowired
	private MqttToolMessageHandler messageHandler;
	
	public boolean isConnected() {
		if(this.client == null) {
			return false;
		} else {
			return this.client.isConnected();			
		}
	}
	
	public synchronized void disconnect() {
		if(this.client == null) {
			return;
		}
		if(this.client.isConnected()) {
			try {
				this.client.disconnectForcibly(5 * 1000L);
			} catch (MqttException e) {}
		}
		try {
			this.client.close();
		} catch (MqttException e) {}
		this.client = null;
	}
	
	public synchronized void connect() throws MqttException {
		if(this.client != null) {
			this.disconnect();
		}
		this.client = new MqttClient(config.getServerURI(), config.getClientId(), new MemoryPersistence());
		this.client.setCallback(this.getMqttCallback());
		this.client.connect(this.getMqttConnectOptions());
		
		logger.info("mqtt connected");
		//重连后订阅主题
		subscribeTopics();
		//调用连接后处理
		this.config.afterReconnect();
	}
	
	/**
	 * 发布消息
	 * @param topic
	 * @param message
	 * @throws MqttPersistenceException
	 * @throws MqttException
	 */
	public void publish(String topic, MqttMessage message) throws MqttPersistenceException, MqttException {
		this.client.publish(topic, message);
		logger.debug("public msg " + topic + " " + (new String(message.getPayload())));
	}
	
	/**
	 * 发布消息
	 * @param topic
	 * @param payload
	 * @param qos
	 * @param retained
	 * @throws MqttException 
	 * @throws MqttPersistenceException 
	 */
	public void publish(String topic, byte[] payload,int qos, boolean retained) throws MqttPersistenceException, MqttException {
		this.client.publish(topic, payload, qos, retained);
		logger.debug("public msg " + topic + " " + (new String(payload)));
	}
	
	private MqttCallback getMqttCallback() {
		return new MqttCallback() {

			@Override
			public void connectionLost(Throwable cause) {
				try {
					config.connectionLost(cause);					
				} catch(Exception e) {
					logger.error(ExceptionTool.getStackTrace(e));
				}
				getReconnectJob().start(config.getReconnectInterval() * 1000);
			}

			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				messageHandler.messageArrived(topic, message);
//				config.messageArrived(topic, new String(message.getPayload()));
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {

			}
			
		};
	}
	
	private MqttConnectOptions getMqttConnectOptions() {
		MqttConnectOptions option = new MqttConnectOptions();
		option.setCleanSession(true);
		if(this.config.getUsername() != null && !"".equals(this.config.getUsername())) {
			option.setUserName(this.config.getUsername());
		}
		if(this.config.getPassword() != null && !"".equals(this.config.getPassword())) {
			option.setPassword(this.config.getPassword().toCharArray());
		}
		if(this.config.getCompletionTimeout() != null) {
			option.setConnectionTimeout(this.config.getCompletionTimeout());
		}
		if(this.config.getReconnectInterval() != null) {
			option.setKeepAliveInterval(this.config.getReconnectInterval());
			option.setAutomaticReconnect(false);
		}
		return option;
	}
	
	public IRunUntillSuccess getReconnectJob() {
		return new IRunUntillSuccess() {

			@Override
			public boolean getPremiss() {
				return config.getPremiss();
			}

			@Override
			public void job() throws Exception {
				try {
					Thread.sleep(config.getReconnectInterval() * 1000);					
				} catch (InterruptedException e) {}
				if(isConnected()) {
					return;
				}
				try {
					connect();					
				} catch (MqttSecurityException e) {
					config.handleConnectError(e);
					throw e;
				} catch (MqttException e) {
					config.handleConnectError(e);
					throw e;
				} catch (Exception e) {
					config.handleConnectError(e);
					throw e;
				}
			}
		};
	}
	
	/**
	 * 订阅主题
	 */
	private void subscribeTopics() {
		if(!isConnected()) {
			return;
		}
		if(config.getSubscribeList() == null) {
			return;
		}
		for(String topic : config.getSubscribeList()) {
			try {
				this.client.subscribe(topic);
				logger.info("mqtt subscribe " + topic);
			} catch (MqttException e) {
				logger.warn(MessageFormat.format("subscribe topic error, topic: {0}, error: {1}", topic, ExceptionTool.getStackTrace(e)));
			}
		}
	}

}
