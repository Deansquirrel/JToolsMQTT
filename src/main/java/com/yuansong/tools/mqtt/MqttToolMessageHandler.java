package com.yuansong.tools.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.yuansong.tools.mqtt.config.IMqttToolConfig;

@Component
public class MqttToolMessageHandler {
	
	@Autowired
	private IMqttToolConfig config;
	
	@Async(value="taskExecutorMqttTool")
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		config.messageArrived(topic, new String(message.getPayload()));
	}

}
