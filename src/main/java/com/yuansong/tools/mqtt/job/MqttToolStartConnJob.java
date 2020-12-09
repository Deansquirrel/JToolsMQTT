package com.yuansong.tools.mqtt.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yuansong.tools.common.IRunUntillSuccess;
import com.yuansong.tools.mqtt.MqttToolClient;

@Component
public class MqttToolStartConnJob implements IRunUntillSuccess {
	
	@Autowired
	private MqttToolClient mqttToolClient;

	@Override
	public boolean getPremiss() {
		return true;
	}

	@Override
	public void job() throws Exception {
		mqttToolClient.getReconnectJob().start();
	}

}
