package com.yuansong.tools.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling // 2.开启定时任务
public class MQTTReconnectTask {
	
	@Scheduled(cron = "0/5 * * * * ?")
	public synchronized void reConnect() {
		MQTTClient client = MQTTClient.getInstance();
		if(client.isReady() && !client.isConnected()) {
			try {
				client.connect();
			} catch (MqttSecurityException e) {
				if(e.getReasonCode() == MqttSecurityException.REASON_CODE_FAILED_AUTHENTICATION) {
					client.handleConnectFailedAuthenticationError();
				}
			} catch (MqttException e) {
			} catch (Exception e) {
			}
		}
	}
}
