package com.github.quickprotocol;

/**
 * QP指令集
 */
public class QpInstruction {
	/**
	 * 指令集编号
	 */
    public String Id;
    /**
     * 指令集名称
     */
    public String Name;
    /**
     * 包含的通知信息数组
     */
    public QpNoticeInfo[] NoticeInfos;
    /**
     * 包含的命令信息数组
     */
    public QpCommandInfo[] CommandInfos;
}
