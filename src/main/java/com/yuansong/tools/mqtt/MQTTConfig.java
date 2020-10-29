package com.yuansong.tools.mqtt;

public class MQTTConfig {
	
	private String username;
	private String password;
	private String hostUrl;
	private String clientId;
	private String defaultTopic;
	private Integer completionTimeout;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getHostUrl() {
		return hostUrl;
	}
	public void setHostUrl(String hostUrl) {
		this.hostUrl = hostUrl;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getDefaultTopic() {
		return defaultTopic;
	}
	public void setDefaultTopic(String defaultTopic) {
		this.defaultTopic = defaultTopic;
	}
	public Integer getCompletionTimeout() {
		return completionTimeout;
	}
	public void setCompletionTimeout(Integer completionTimeout) {
		this.completionTimeout = completionTimeout;
	}
}
