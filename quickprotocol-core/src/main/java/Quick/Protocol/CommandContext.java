package Quick.Protocol;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.commons.codec.binary.Hex;

import Quick.Protocol.Exceptions.CommandException;
import Quick.Protocol.Utils.BitConverter;

public class CommandContext {
	public String Id;
	private CommandException commandException;
	private boolean isTimeout = false;
	private CommandResponseTypeNameAndContent response;
	public FutureTask<CommandResponseTypeNameAndContent> ResponseTask;

	public CommandContext(final String typeName) {
		Id = Hex.encodeHexString(BitConverter.GetBytes(UUID.randomUUID())).toLowerCase();
		ResponseTask = new FutureTask<CommandResponseTypeNameAndContent>(
				new Callable<CommandResponseTypeNameAndContent>() {
					public CommandResponseTypeNameAndContent call() {
						if (isTimeout)
							throw new RuntimeException(
									String.format("Command[Id:%s,Type:%s] is timeout.", Id, typeName));
						if (commandException != null)
							throw commandException;
						return response;
					}
				});
	}

	public void SetResponse(CommandException commandException) {
		if (isTimeout)
			return;
		this.commandException = commandException;
		ResponseTask.run();
	}

	public void SetResponse(String responseTypeName, String responseContent) {
		if (isTimeout)
			return;

		this.response = new CommandResponseTypeNameAndContent();
		this.response.TypeName = responseTypeName;
		this.response.Content = responseContent;

		ResponseTask.run();
	}

	public void Timeout() {
		isTimeout = true;
		ResponseTask.run();
	}
}
