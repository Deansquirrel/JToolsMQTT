package com.yuansong.tools.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public interface IMQTTHelper {
	
	/**
	 * 更新配置
	 * @param config 连接配置
	 */
	public void updateConfig(MQTTConfig config);
	
	/**
	 * 更新回调
	 * @param callback
	 */
	public void updateCallback(MQTTCallback callback);
	
	/**
	 * 是否已连接
	 * @return
	 */
	public boolean isConnected();
	
	/**
	 * 连接
	 * @throws MqttSecurityException
	 * @throws MqttException
	 * @throws Exception
	 */
	public void connect() throws MqttSecurityException, MqttException, Exception;
	
	/**
	 * 启动线程，轮询配置
	 * 线程停止条件：直至发现有一次成功的连接
	 */
	public void startWaitForConn();
	
	/**
	 * 连接
	 * @param config
	 * @throws MqttSecurityException
	 * @throws MqttException
	 * @throws Exception
	 */
	default public void connect(MQTTConfig config) throws MqttSecurityException, MqttException, Exception {
		this.updateConfig(config);
		this.connect();
	}
	
	/**
	 * 发布消息
	 * @param topic
	 * @param data
	 */
	default public void publish(String topic, String data) throws MqttException, MqttPersistenceException, Exception{
		this.publish(topic, data, 0, false);
	}
	
	/**
	 * 发布消息
	 * @param topic
	 * @param data
	 * @param qos
	 */
	default public void publish(String topic, String data, int qos) throws MqttException, MqttPersistenceException, Exception{
		this.publish(topic, data, qos, false);
	}
	
	/**
	 * 发布消息
	 * @param topic
	 * @param data
	 * @param qos
	 * @param retained
	 */
	public void publish(String topic, String data, int qos, boolean retained) throws MqttException, MqttPersistenceException, Exception;
	
	/**
	 * 订阅消息
	 * @param topic
	 */
	default public void subscribe(String topic) throws MqttException, Exception{
		this.subscribe(topic, 0);
	}
	
	/**
	 * 订阅消息
	 * @param topic
	 * @param qos
	 */
	public void subscribe(String topic, int qos) throws MqttException, Exception;

	/**
	 * 取消订阅
	 * @param topic
	 * @throws MqttException
	 */
	public void unsubscribe(String topic) throws MqttException;

}
