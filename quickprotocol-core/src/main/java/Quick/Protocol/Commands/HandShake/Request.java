package Quick.Protocol.Commands.HandShake;

import com.github.quickprotocol.DisplayName;
import com.github.quickprotocol.IQpCommandRequest;

@DisplayName("握手")
public class Request implements IQpCommandRequest<Response> {
	/// <summary>
	/// 传输超时(默认15秒)
	/// </summary>
	public int TransportTimeout = 15000;
	/// <summary>
	/// 启用加密(默认为false)
	/// </summary>
	public Boolean EnableEncrypt = false;
	/// <summary>
	/// 启用压缩(默认为false)
	/// </summary>
	public Boolean EnableCompress = false;
}