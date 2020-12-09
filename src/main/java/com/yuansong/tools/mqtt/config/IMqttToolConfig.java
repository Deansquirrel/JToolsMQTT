package com.yuansong.tools.mqtt.config;

import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public interface IMqttToolConfig {

	public String getServerURI();
	
	public String getClientId();
	
	public String getUsername();
	
	public String getPassword();
	
	public Integer getCompletionTimeout();
	
	public List<String> getSubscribeList();
	
	//重连间隔（秒）
	public Integer getReconnectInterval();
	
	/**
	 * 是否可以启动连接
	 * @return
	 */
	public boolean getPremiss();
	
	/**
	 * 连接断开
	 * @param cause
	 */
	public void connectionLost(Throwable cause);
	
	/**
	 * 断线重连后的处理（含首次连接）
	 */
	public void afterReconnect();
	
	/**
	 * 订阅后收到的消息处理
	 * @param topic
	 * @param message
	 * @throws Exception
	 */
	public void messageArrived(String topic, String message) throws Exception;
	
	/**
	 * 连接错误处理
	 * @param e
	 */
	public void handleConnectError(MqttSecurityException e);
	public void handleConnectError(MqttException e);
	public void handleConnectError(Exception e);
}
