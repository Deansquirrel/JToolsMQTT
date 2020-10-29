package com.yuansong.tools.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MQTTClient {
	
	private static final Logger logger = LoggerFactory.getLogger(MQTTClient.class);
	
	private MqttClient client = null;
	private static volatile MQTTClient mqttClient = null;
	
	private MQTTConfig config = null;
	private MQTTCallback callback = null;
	
	
	public static MQTTClient getInstance() {
		if(mqttClient == null) {
			synchronized (MQTTClient.class ) {
				if(mqttClient == null) {
					mqttClient = new MQTTClient();
				}
			}
		}
		return mqttClient;
	}
	
	private MQTTClient() {
		
	}
	
	public boolean isReady() {
		return this.config != null;
	}
	
	/**
	 * 更新配置
	 * @param config
	 * @param callback
	 */
	public void updateConfig(MQTTConfig config, MQTTCallback callback) {
		this.config = config;
		this.callback = callback;
	}
	
	/**
	 * 是否已连接
	 * @return
	 */
	public boolean isConnected() {
		if(this.client == null) {
			return false;
		} else {
			return this.client.isConnected();
		}
	}
	
	/**
	 * 断开连接
	 */
	public void disconnect() {
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
	
	/**
	 * 连接
	 * @throws MqttSecurityException
	 * @throws MqttException
	 * @throws Exception
	 */
	public synchronized void connect() throws MqttSecurityException, MqttException, Exception {
		if(!this.isReady()) {
			logger.warn("config is null");
			throw new Exception("not ready, config is null");
		}
		if(this.client != null && this.client.isConnected()) {
			return;
		}
		
		this.client = new MqttClient(this.config.getHostUrl(), this.config.getClientId(), new MemoryPersistence());
		
		this.client.setCallback(new MqttCallback() {
			@Override
			public void connectionLost(Throwable cause) {
				logger.warn("MQTT connection lost");
				if(callback != null) {
					callback.connectionLost(cause);
				}
			}
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				if(callback != null) {
					callback.messageArrived(topic, message);
				}
			}
			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {}
		});
		
		MqttConnectOptions option = this.getMqttConnectOptions();
		try {
			this.client.connect(option);
		} catch (MqttSecurityException e) {
			if(e.getReasonCode() == MqttSecurityException.REASON_CODE_FAILED_AUTHENTICATION) {
				if(this.callback != null) {
					this.callback.handleConnectFailedAuthenticationError();
					return;
				}
			} 
			throw e;
		} 
		
		logger.info("mqtt connected");
		if(callback != null) {
			callback.afterReconnect();
		}
	}
	
	private MqttConnectOptions getMqttConnectOptions() {
		if(this.config == null) {
			return null;
		}
		MqttConnectOptions option = new MqttConnectOptions();
		option.setCleanSession(true);
		if(this.config.getUsername() != null) {
			option.setUserName(this.config.getUsername());			
		}
		if(this.config.getPassword() != null) {
			option.setPassword(this.config.getPassword().toCharArray());			
		}
		if(this.config.getCompletionTimeout() != null) {
			option.setConnectionTimeout(this.config.getCompletionTimeout());
		}
		option.setKeepAliveInterval(5);
		option.setAutomaticReconnect(true);
		return option;
	}

	public void publish(String topic, String data, int qos, boolean retained) 
			throws MqttException, MqttPersistenceException {
		if(data == null) {
			data = "";
		}
		MqttMessage message = new MqttMessage();
		message.setQos(qos);
		message.setRetained(retained);
		message.setPayload(data.getBytes());
		MqttTopic mqttTopic = this.client.getTopic(topic);
		MqttDeliveryToken token;
		token = mqttTopic.publish(message);
		token.waitForCompletion();		
	}
	
	public void subscribe(String topic, int qos) throws MqttException {
		this.client.subscribe(topic, qos);
	}
	
	public void unsubscribe(String topic) throws MqttException {
		this.client.unsubscribe(topic);
	}
	
	public void handleConnectFailedAuthenticationError() {
		if(callback != null) {
			callback.handleConnectFailedAuthenticationError();
		}
	}
	
}
