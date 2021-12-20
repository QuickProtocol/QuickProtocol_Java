package Quick.Protocol.SerialPort;

import com.fazecast.jSerialComm.SerialPort;

import Quick.Protocol.QpServerOptions;

public class QpSerialPortServerOptions extends QpServerOptions {
	/// <summary>
	/// 端口名称
	/// </summary>
	public String PortName = "COM1";
	/// <summary>
	/// 波特率
	/// </summary>
	public int BaudRate = 9600;
	/// <summary>
	/// 奇偶校验位
	/// </summary>
	public int Parity = SerialPort.NO_PARITY;
	/// <summary>
	/// 数据位
	/// </summary>
	public int DataBits = 8;
	/// <summary>
	/// 停止位
	/// </summary>
	public int StopBits = SerialPort.ONE_STOP_BIT;

	@Override
	public void Check() {
		super.Check();
		if (PortName == null || PortName.equals(""))
			throw new RuntimeException("PortName is null");
	}
}
