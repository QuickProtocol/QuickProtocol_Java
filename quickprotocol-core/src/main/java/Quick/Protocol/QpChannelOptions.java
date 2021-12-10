package Quick.Protocol;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class QpChannelOptions {

	/**
	 * 心跳间隔，为发送或接收超时中小的值的三分一
	 */
	@JsonIgnore()
	public int getHeartBeatInterval() {
		return InternalTransportTimeout / 3;
	}

	/**
	 * 密码
	 */
	public String Password = "HelloQP";

	@JsonIgnore()
	private QpInstruction[] _InstructionSet = new QpInstruction[] { Quick.Protocol.Base.getInstruction() };

	/**
	 * 支持的指令集
	 */
	@JsonIgnore()
	public QpInstruction[] getInstructionSet() {
		return _InstructionSet;
	}

	@JsonIgnore()
	public void setInstructionSet(QpInstruction[] value) {
		_InstructionSet = value;
		QpInstruction baseInstruction = Quick.Protocol.Base.getInstruction();
		ArrayList<QpInstruction> list = new ArrayList<QpInstruction>();
		list.add(baseInstruction);
		for (QpInstruction item : value) {
			if (item.Id == baseInstruction.Id)
				return;
			list.add(item);
		}
		_InstructionSet = list.toArray(new QpInstruction[0]);
	}

	/**
	 * 内部是否压缩
	 */
	protected boolean InternalCompress = false;
	/**
	 * 内部是否加密
	 */

	protected boolean InternalEncrypt = false;
	/**
	 * 内部接收超时(默认15秒)
	 */
	protected int InternalTransportTimeout = 15 * 1000;

	/**
	 * 最大包大小(默认为：10MB)
	 */
	public int MaxPackageSize = 10 * 1024 * 1024;

	public void Check() {
		if (InternalTransportTimeout <= 0)
			throw new RuntimeException("TransportTimeout must larger than 0");

		if (Password == null)
			throw new java.lang.IllegalArgumentException("Password");
	}

	/**
	 * 是否触发NoticePackageReceived事件
	 */
	public boolean RaiseNoticePackageReceivedEvent = true;

	/**
	 * 指令执行器管理器列表
	 */
	public List<CommandExecuterManager> CommandExecuterManagerList = new ArrayList<CommandExecuterManager>();

	/**
	 * 注册指令执行器管理器
	 * 
	 * @param commandExecuterManager
	 */
	public void RegisterCommandExecuterManager(CommandExecuterManager commandExecuterManager) {
		CommandExecuterManagerList.add(commandExecuterManager);
	}
}
