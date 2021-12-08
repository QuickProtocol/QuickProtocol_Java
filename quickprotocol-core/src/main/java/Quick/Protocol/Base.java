package Quick.Protocol;

import java.util.ArrayList;

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
			cmdList.add(QpCommandInfo.Create(new Quick.Protocol.Commands.HandShake.Request(),
					Quick.Protocol.Commands.HandShake.Response.class));
			cmdList.add(QpCommandInfo.Create(new Quick.Protocol.Commands.PrivateCommand.Request(),
					Quick.Protocol.Commands.PrivateCommand.Response.class));
			cmdList.add(QpCommandInfo.Create(new Quick.Protocol.Commands.GetQpInstructions.Request(),
					Quick.Protocol.Commands.GetQpInstructions.Response.class));
			_Instruction.CommandInfos = cmdList.toArray(new QpCommandInfo[0]);

			ArrayList<QpNoticeInfo> noticeList = new ArrayList<QpNoticeInfo>();
			noticeList.add(QpNoticeInfo.Create(Quick.Protocol.Notices.PrivateNotice.class));
			_Instruction.NoticeInfos = noticeList.toArray(new QpNoticeInfo[0]);
		}
		return _Instruction;
	}

}
