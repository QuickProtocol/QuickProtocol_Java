package Quick.Protocol.Pipeline;

import Quick.Protocol.QpClientOptions;

public class QpPipelineClientOptions extends QpClientOptions {
	/**
	 * 服务器名称
	 */
	public String ServerName = ".";

	/**
	 * 管道名称
	 */
	public String PipeName = "Quick.Protocol";

	@Override
	public void Check() {
		super.Check();
		if (PipeName == null || PipeName.equals(""))
			throw new RuntimeException("PipeName is null.");
	}

	@Override
	public String GetConnectionInfo() {
		return String.format("%s\\%s", ServerName, PipeName);
	}
}
