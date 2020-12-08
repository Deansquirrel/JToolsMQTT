package com.yuansong.tools.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface IMqttToolCallback {
	
	/**
	 * 连接断开
	 * @param cause
	 */
	public void connectionLost(Throwable cause);
	
	/**
	 * 订阅后收到的消息处理
	 * @param topic
	 * @param message
	 * @throws Exception
	 */
	public void messageArrived(String topic, MqttMessage message) throws Exception;
	
	/**
	 * 断线重连后的处理（含首次连接）
	 */
	public void afterReconnect();
	
	/**
	 * 用户名密码错误的处理过程
	 */
	public void handleConnectFailedAuthenticationError();

}
