package Quick.Protocol.Commands.HandShake;

import Quick.Protocol.IQpCommandRequest;
import Quick.Protocol.Annotations.DisplayName;

@DisplayName("握手")
public class Request implements IQpCommandRequest<Response> {
	/// <summary>
	/// 传输超时(默认15秒)
	/// </summary>
	public int TransportTimeout = 15000;
	/// <summary>
	/// 启用加密(默认为false)
	/// </summary>
	public boolean EnableEncrypt = false;
	/// <summary>
	/// 启用压缩(默认为false)
	/// </summary>
	public boolean EnableCompress = false;
}