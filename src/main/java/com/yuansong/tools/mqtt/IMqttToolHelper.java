package com.yuansong.tools.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

public interface IMqttToolHelper {

	public boolean isConnected();
	
	default public void publish(String topic, String data) throws MqttPersistenceException, MqttException {
		this.publish(topic, data, 0, false);
	}
	
	default public void publish(String topic, String data, int qos, boolean retained) throws MqttPersistenceException, MqttException {
		this.publish(topic, data.getBytes(), qos, retained);
	}
	
	public void publish(String topic, byte[] payload,int qos, boolean retained) throws MqttPersistenceException, MqttException;
	
}
