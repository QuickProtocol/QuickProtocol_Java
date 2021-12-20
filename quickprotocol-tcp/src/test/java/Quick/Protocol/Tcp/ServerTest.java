package Quick.Protocol.Tcp;

import java.net.InetAddress;
import java.util.Date;

import Quick.Protocol.QpServerChannel;
import Quick.Protocol.Listeners.ServerChannelConnectedListener;
import Quick.Protocol.Listeners.ServerChannelDisconnectedListener;
import Quick.Protocol.Utils.LogUtils;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ServerTest extends TestCase {
	public ServerTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(ServerTest.class);
	}

	public void testApp() {
		Quick.Protocol.Utils.LogUtils.LogConnection = true;
		Quick.Protocol.Utils.LogUtils.LogHeartbeat = true;
		Quick.Protocol.Utils.LogUtils.LogPackage = true;
		Quick.Protocol.Utils.LogUtils.LogContent = true;
		Quick.Protocol.Utils.LogUtils.LogSplit = true;
		Quick.Protocol.Utils.LogUtils.LogCommand = true;
		Quick.Protocol.Utils.LogUtils.SetConsoleLogHandler();

		Quick.Protocol.Tcp.QpTcpServerOptions options = new Quick.Protocol.Tcp.QpTcpServerOptions();
		options.Address = InetAddress.getLoopbackAddress();
		options.Port = 3011;
		options.Password = "HelloQP";
		options.ServerProgram = "QuickProtocol Server Test(Java)";

		Quick.Protocol.Tcp.QpTcpServer server = new Quick.Protocol.Tcp.QpTcpServer(options);
		server.addChannelConnectedListener(new ServerChannelConnectedListener() {

			public void Invoke(QpServerChannel channel) {
				Server_ChannelConnected(channel);
			}

		});
		server.addChannelDisconnectedListener(new ServerChannelDisconnectedListener() {

			public void Invoke(QpServerChannel channel) {
				Server_ChannelDisconnected(channel);
			}
		});
		try {
			server.Start();
			LogUtils.Log("服务启动成功!");
			System.in.read();
		} catch (Exception ex) {
			LogUtils.Log("服务启动失败!" + ex.toString());
		}
		server.Stop();
	}

	private static void Server_ChannelConnected(QpServerChannel channel) {
		LogUtils.Log("%s: 通道[%s]已连接!", new Date().toString(), channel.ChannelName);
	}

	private static void Server_ChannelDisconnected(QpServerChannel channel) {
		LogUtils.Log("%s: 通道[%s]已断开!", new Date().toString(), channel.ChannelName);
	}
}
