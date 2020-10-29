package com.yuansong.tools.mqtt;

import java.text.MessageFormat;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MQTTHelperImpl implements MQTTHelper {
	
	private MQTTClient client = MQTTClient.getInstance();
	
	private static final Logger logger = LoggerFactory.getLogger(MQTTHelper.class);

	@Override
	public void updateConfig(MQTTConfig config, MQTTCallback callback) {
		this.client.updateConfig(config, callback);
	}

	@Override
	public boolean isConnected() {
		return this.client.isConnected();
	}

	@Override
	public void connect() throws MqttSecurityException, MqttException, Exception {
		this.client.connect();
	}

	@Override
	public void publish(String topic, String data, int qos, boolean retained) throws MqttException, MqttPersistenceException, Exception{
		if(!this.client.isConnected()) {
			this.client.connect();
		}
		if(this.client.isConnected()) {
			this.client.publish(topic, data, qos, retained);			
		} else {
			String errMsg = MessageFormat.format("send error, connect is not ready. Topic: {0} ,data: {1}", topic, data);
			logger.warn(errMsg);
			throw new Exception(errMsg);
		}
	}

	@Override
	public void subscribe(String topic, int qos) throws MqttException, Exception {
		if(!this.client.isConnected()) {
			this.client.connect();
		}
		if(this.client.isConnected()) {
			this.client.subscribe(topic, qos);			
		} else {
			String errMsg = MessageFormat.format("subscribe topic error, connect is not ready. Topic: {0}", topic);
			logger.warn(errMsg);
			throw new Exception(errMsg);
		}
	}

	@Override
	public void unsubscribe(String topic) throws MqttException {
		if(this.client.isConnected()) {
			this.client.unsubscribe(topic);
		}
	}
	
}
