package Quick.Protocol;

public interface ICommandExecuter<TCmdRequest, TCmdResponse> {
	public TCmdResponse Execute(QpChannel handler, TCmdRequest request);
}
