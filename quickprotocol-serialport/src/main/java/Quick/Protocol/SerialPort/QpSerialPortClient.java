package Quick.Protocol.SerialPort;

import org.apache.commons.codec.binary.StringUtils;

import com.fazecast.jSerialComm.SerialPort;

import Quick.Protocol.ConnectionStreamInfo;
import Quick.Protocol.QpClient;
import Quick.Protocol.QpConsts;

public class QpSerialPortClient extends QpClient {

	private QpSerialPortClientOptions options;
	private SerialPort serialPort;

	public QpSerialPortClient(QpSerialPortClientOptions options) {
		super(options);
		this.options = options;
	}

	@Override
	protected ConnectionStreamInfo InnerConnectAsync() {
		serialPort = SerialPort.getCommPort(options.PortName);
		serialPort.setComPortParameters(options.BaudRate, options.DataBits, options.StopBits, options.Parity);
		serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, options.getTransportTimeout(),options.getTransportTimeout());
		if (!serialPort.openPort())
			throw new RuntimeException("Serial port open error.");
		ConnectionStreamInfo csi = new ConnectionStreamInfo();
		csi.ConnectionInputStream = serialPort.getInputStream();
		csi.ConnectionOutputStream = serialPort.getOutputStream();

		byte[] tmpBuffer = StringUtils.getBytesUtf8(QpConsts.QuickProtocolNameAndVersion + "\n");
		serialPort.writeBytes(tmpBuffer, tmpBuffer.length);
		return csi;
	}

	@Override
	protected void Disconnect() {
		if (serialPort != null) {
			try {
				serialPort.closePort();
			} catch (Exception ex) {
			}
			serialPort = null;
		}

		super.Disconnect();
	}
}
