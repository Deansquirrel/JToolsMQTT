package com.yuansong.tools.mqtt.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.yuansong.tools.mqtt.job.MqttToolStartConnJob;

@Component
public class MqttToolInit implements CommandLineRunner {
	
	@Autowired
	private MqttToolStartConnJob mqttToolStartConnJob;

	@Override
	public void run(String... args) throws Exception {
		this.mqttToolStartConnJob.start();
	}

}
