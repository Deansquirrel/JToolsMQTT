package com.yuansong.tools.mqtt.task;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class MqttToolTaskExecutor {

	@Bean(name="taskExecutorMqttTool")
	public Executor mqttTaskExecutor() {
	   ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
	   executor.setCorePoolSize(1);
	   executor.setMaxPoolSize(10);
	   executor.setQueueCapacity(200);

	   executor.setKeepAliveSeconds(60);
	   executor.setThreadNamePrefix("taskExecutorMqttTool-");
	   executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
	   executor.setWaitForTasksToCompleteOnShutdown(true);
	   executor.setAwaitTerminationSeconds(60);
	    return executor;
	}
	
}
