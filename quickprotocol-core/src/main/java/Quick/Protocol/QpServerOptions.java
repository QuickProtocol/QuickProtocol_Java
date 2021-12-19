package Quick.Protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;

import Quick.Protocol.Utils.JsonConvert;

public abstract class QpServerOptions extends QpChannelOptions {
	/**
	 * 缓存大小(默认128KB)
	 */
	public int BufferSize = 128 * 1024;
	/**
	 * 认证超时时间，在指定的超时时间没有完成认证，则断开连接
	 */
	public int AuthenticateTimeout = 5000;
	/**
	 * 服务端程序
	 */
	public String ServerProgram;

	/**
	 * 协议错误处理器
	 */
	@JsonIgnore
	public ProtocolErrorHandler ProtocolErrorHandler;

	public QpServerOptions Clone() {
		QpServerOptions ret = (QpServerOptions) JsonConvert.DeserializeObject(JsonConvert.SerializeObject(this),
				this.getClass());
		ret.setInstructionSet(getInstructionSet());
		ret.CommandExecuterManagerList = CommandExecuterManagerList;
		ret.ProtocolErrorHandler = ProtocolErrorHandler;
		return ret;
	}
}