package Quick.Protocol;

/**
 * 数据包类型
 */
public class QpPackageType {
	/**
	 * 心跳数据包
	 */
	public final static byte Heartbeat = 0;
	/**
	 * 通知数据包
	 */
	public final static byte Notice = 1;
	/**
	 * 指令请求数据包
	 */
	public final static byte CommandRequest = 2;
	/**
	 * 指令响应数据包
	 */
	public final static byte CommandResponse = 3;
	/**
	 * 拆分数据包
	 */
	public final static byte Split = (byte) 255;
}
