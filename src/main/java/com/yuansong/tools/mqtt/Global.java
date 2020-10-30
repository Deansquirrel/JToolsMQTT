package com.yuansong.tools.mqtt;

class Global {

	//回调接口
	public static volatile MQTTCallback callback = null;
	//重连间隔（秒）
	public static int reconnectInterval = 5;
	
}
