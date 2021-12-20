package Quick.Protocol.SerialPort;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.fazecast.jSerialComm.SerialPort;

import Quick.Protocol.ConnectionStreamInfo;
import Quick.Protocol.QpConsts;
import Quick.Protocol.QpServer;
import Quick.Protocol.QpServerChannel;
import Quick.Protocol.Listeners.ServerChannelDisconnectedListener;
import Quick.Protocol.Utils.CancellationToken;
import Quick.Protocol.Utils.LogUtils;

public class QpSerialPortServer extends QpServer implements ServerChannelDisconnectedListener {
	private QpSerialPortServerOptions options;
	private SerialPort serialPort;
	private boolean isAccepted = false;
	private Charset encoding = Charset.forName("utf-8");

	public QpSerialPortServer(QpSerialPortServerOptions options) {
		super(options);
		this.options = options;
	}

	@Override
	public void Start() {
		super.addChannelDisconnectedListener(this);
		if (LogUtils.LogConnection)
			LogUtils.Log("Opening SerialPort[%s]...", options.PortName);
		serialPort = SerialPort.getCommPort(options.PortName);
		serialPort.setComPortParameters(options.BaudRate, options.DataBits, options.StopBits, options.Parity);
		serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 15000, 15000);
		if (!serialPort.openPort())
			throw new RuntimeException("Serial port open error.");

		if (LogUtils.LogConnection)
			LogUtils.Log("SerialPort[%s] open success.", options.PortName);
		isAccepted = false;
		super.Start();
	}

	@Override
	public void Stop() {
		super.Stop();
		if (serialPort != null) {
			try {
				serialPort.closePort();
			} catch (Exception ex) {
			}
			serialPort = null;
		}
		super.removeChannelDisconnectedListener(this);
	}

	@Override
	protected void InnerAcceptAsync(CancellationToken token) {
		if (isAccepted) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				return;
			}
			return;
		}
		isAccepted = true;

		if (!serialPort.isOpen())
			serialPort.openPort();

		byte[] buffer;
		try {
			buffer = serialPort.getInputStream().readNBytes(QpConsts.QuickProtocolNameAndVersion.length() + 1);
		} catch (IOException e) {
			return;
		}
		if (token.IsCancellationRequested())
			return;
		String line = encoding.decode(ByteBuffer.wrap(buffer, 0, buffer.length - 1)).toString();

		if (!QpConsts.QuickProtocolNameAndVersion.equals(line)) {
			isAccepted = false;
			if (LogUtils.LogConnection)
				LogUtils.Log("[Connection]Protocol not match.Recv Data:" + line);
			return;
		}
		ConnectionStreamInfo csi = new ConnectionStreamInfo();
		csi.ConnectionInputStream = serialPort.getInputStream();
		csi.ConnectionOutputStream = serialPort.getOutputStream();
		OnNewChannelConnected(csi, "SerialPort:" + options.PortName, token);
	}

	@Override
	public void Invoke(QpServerChannel channel) {
		isAccepted = false;
	}

}
