package com.yuansong.tools.mqtt;

class Global {

	//重连间隔（秒）
	public static int reconnectInterval = 5;
		
	//回调接口
	public static volatile IMqttToolCallback callback = null;
}
