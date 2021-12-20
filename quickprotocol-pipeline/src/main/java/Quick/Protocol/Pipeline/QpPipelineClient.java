package Quick.Protocol.Pipeline;

import java.io.RandomAccessFile;

import Quick.Protocol.ConnectionStreamInfo;
import Quick.Protocol.QpClient;

public class QpPipelineClient extends QpClient {
	private QpPipelineClientOptions options;
	private RandomAccessFile pipe;

	public QpPipelineClient(QpPipelineClientOptions options) {
		super(options);
		this.options = options;
	}

	@Override
	protected ConnectionStreamInfo InnerConnectAsync() {
		try {
			pipe = new RandomAccessFile(String.format("\\\\%s\\pipe\\%s", options.ServerName, options.PipeName), "rw");
			ConnectionStreamInfo csi = new ConnectionStreamInfo();
			csi.ConnectionInputStream = new RandomAccessFileInputStream(pipe);
			csi.ConnectionOutputStream = new RandomAccessFileOutputStream(pipe);
			return csi;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void Disconnect() {
		if (pipe != null) {
			try {
				pipe.close();
			} catch (Exception ex) {
			}
			pipe = null;
		}

		super.Disconnect();
	}
}