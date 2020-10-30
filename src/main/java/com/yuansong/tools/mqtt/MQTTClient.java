package com.yuansong.tools.mqtt;

import java.text.MessageFormat;

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
	
	//是否有等待连接线程
	private volatile boolean hasWaitForConn = false;
	
	/**
	 * 获取唯一对象
	 * @return
	 */
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
	
	private MQTTClient() {	}
	
	/**
	 * 是否就绪（可连接）
	 * @return
	 */
	public boolean isReady() {
		return this.config != null;
	}
	
	/**
	 * 更新配置
	 * @param config
	 */
	public void updateConfig(MQTTConfig config) {
		this.config = config;
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
		this.disconnect();
		this.client = new MqttClient(this.config.getHostUrl(), this.config.getClientId(), new MemoryPersistence());
		this.client.setCallback(this.getMqttCallback());
		
		MqttConnectOptions option = this.getMqttConnectOptions();
		
		this.client.connect(option);
		
		logger.info("mqtt connected");
		//调用连接后处理
		if(Global.callback != null) {
			Global.callback.afterReconnect();
		}
	}
	
	/**
	 * 启动线程，轮询配置
	 * 线程停止条件：直至发现有一次成功的连接
	 */
	public void connectUntilSuccess() {
		if(this.hasWaitForConn) {
			return;
		}
		this.hasWaitForConn = true;
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				MQTTClient client = MQTTClient.getInstance();
				while(true) {
					//等待特定时间
					try {
						Thread.sleep(Global.reconnectInterval);
					} catch (InterruptedException e) {}
					if(client.isConnected()) {
						//已连接则退出
						continue;
					}
					if(!client.isReady()) {
						//连接准备未就绪
						continue;
					}
					//连接已就绪，发起连接
					try {
						client.connect();
						break;
					} catch (MqttSecurityException e) {
						if(MqttSecurityException.REASON_CODE_FAILED_AUTHENTICATION == e.getReasonCode()) {
							if(Global.callback != null) {
								Global.callback.handleConnectFailedAuthenticationError();
							}
						}
						continue;
					} catch (MqttException e) {
						continue;
					} catch (Exception e) {
						continue;
					}
				}
				hasWaitForConn = false;
			}
		});
		t.start();
		logger.debug("start connect until success");
	}

	/**
	 * 发布消息
	 * @param topic
	 * @param data
	 * @throws MqttException
	 * @throws MqttPersistenceException
	 */
	public void publish(String topic, String data) 
			throws MqttException, MqttPersistenceException {
		this.publish(topic, data, 0, false);
	}
	
	/**
	 * 发布消息
	 * @param topic
	 * @param data
	 * @param qos
	 * @param retained
	 * @throws MqttException
	 * @throws MqttPersistenceException
	 */
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
	
	/**
	 * 订阅消息
	 * @param topic
	 * @param qos
	 * @throws MqttException
	 */
	public void subscribe(String topic, int qos) throws MqttException {
		this.client.subscribe(topic, qos);
		logger.info(MessageFormat.format("mqt subscribe topic: {0}, qos: {1}", topic, qos));
	}
	
	/**
	 * 取消订阅
	 * @param topic
	 * @throws MqttException
	 */
	public void unsubscribe(String topic) throws MqttException {
		this.client.unsubscribe(topic);
		logger.info(MessageFormat.format("mqt unsubscribe topic: {0}", topic));
	}
	
	/**
	 * 获取MqttClient连接配置
	 * @return
	 */
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
		option.setKeepAliveInterval(Global.reconnectInterval);
		option.setAutomaticReconnect(false);
		return option;
	}
	
	/**
	 * 获取MqttCallback回调配置
	 * @return
	 */
	private MqttCallback getMqttCallback() {
		return new MqttCallback() {
			@Override
			public void connectionLost(Throwable cause) {
				logger.warn("mqtt connection lost");
				MQTTClient client = MQTTClient.getInstance();
				client.connectUntilSuccess();
			}
			
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				if(Global.callback != null) {
					Global.callback.messageArrived(topic, message);
				}
			}
			
			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {}
		};
	}
	
}
