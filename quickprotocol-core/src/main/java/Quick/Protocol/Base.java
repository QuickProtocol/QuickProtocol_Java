package Quick.Protocol;

import java.util.ArrayList;

import com.github.quickprotocol.QpCommandInfo;
import com.github.quickprotocol.QpInstruction;

public class Base {
	private static QpInstruction _Instruction = null;

	public static QpInstruction getInstruction() {
		if (_Instruction == null) {
			_Instruction = new QpInstruction();
			_Instruction.Id = Base.class.getName();
			_Instruction.Name = "基础指令集";

			ArrayList<QpCommandInfo> cmdList = new ArrayList<QpCommandInfo>();
			cmdList.add(QpCommandInfo.Create(new Quick.Protocol.Commands.Connect.Request(),
					Quick.Protocol.Commands.Connect.Response.class));
			cmdList.add(QpCommandInfo.Create(new Quick.Protocol.Commands.Authenticate.Request(),
					Quick.Protocol.Commands.Authenticate.Response.class));
			_Instruction.CommandInfos = cmdList.toArray(new QpCommandInfo[0]);
		}
		return _Instruction;
		/*
		 * new QpInstruction() { Id = typeof(Base).FullName, Name = "基础指令集", NoticeInfos
		 * = new QpNoticeInfo[] { QpNoticeInfo.Create(new Notices.PrivateNotice()) },
		 * CommandInfos = new QpCommandInfo[] { QpCommandInfo.Create(new
		 * Commands.Connect.Request()), QpCommandInfo.Create(new
		 * Commands.Authenticate.Request()), QpCommandInfo.Create(new
		 * Commands.HandShake.Request()), QpCommandInfo.Create(new
		 * Commands.PrivateCommand.Request()), QpCommandInfo.Create(new
		 * Commands.GetQpInstructions.Request()), } };
		 */
	}

}
