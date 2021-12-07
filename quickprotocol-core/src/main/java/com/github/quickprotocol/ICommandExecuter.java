package com.github.quickprotocol;

public interface ICommandExecuter<TCmdRequest, TCmdResponse> {
	public TCmdResponse execute(QpChannel handler, TCmdRequest request);
}
