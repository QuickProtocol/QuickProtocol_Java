package Quick.Protocol.SerialPort;

import com.fazecast.jSerialComm.SerialPort;

import Quick.Protocol.QpClientOptions;

public class QpSerialPortClientOptions extends QpClientOptions {
	/**
	 * 端口名称
	 */
	public String PortName = "COM1";
	/**
	 * 波特率
	 */
	public int BaudRate = 9600;
	/**
	 * 奇偶校验位
	 */
	public int Parity = SerialPort.NO_PARITY;
	/**
	 * 数据位
	 */
	public int DataBits = 8;
	/**
	 * 停止位
	 */
	public int StopBits = SerialPort.ONE_STOP_BIT;

	@Override
	public void Check() {
		super.Check();
		if (PortName == null || PortName.equals(""))
			throw new RuntimeException("PortName is null.");

	}

	@Override
	public String GetConnectionInfo() {
		return PortName;
	}
}
