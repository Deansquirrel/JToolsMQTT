package com.yuansong.tools.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class MqttToolHelperImpl implements IMqttToolHelper {
	
	@Autowired
	private MqttToolClient mqttToolClient;

	@Override
	public boolean isConnected() {
		return this.mqttToolClient.isConnected();
	}

	@Override
	public void publish(String topic, byte[] payload, int qos, boolean retained) throws MqttPersistenceException, MqttException {
		this.mqttToolClient.publish(topic, payload, qos, retained);
	}

}
