package com.yuansong.tools.mqtt.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yuansong.tools.common.IRunUntillSuccess;
import com.yuansong.tools.mqtt.MqttToolClient;
import com.yuansong.tools.mqtt.config.IMqttToolConfig;

@Component
public class MqttToolStartConnJob implements IRunUntillSuccess {
	
	@Autowired
	private IMqttToolConfig config;
	
	@Autowired
	private MqttToolClient mqttToolClient;

	@Override
	public boolean getPremiss() {
		return config.getPremiss();
	}

	@Override
	public void job() throws Exception {
		mqttToolClient.connect();
	}

}
