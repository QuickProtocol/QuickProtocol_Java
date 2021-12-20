package com.github.quickprotocol.quickprotocol_pipeline_test_client;

import java.io.IOException;

import Quick.Protocol.Listeners.DisconnectedListener;
import Quick.Protocol.Listeners.NoticePackageReceivedListener;
import Quick.Protocol.Listeners.RawNoticePackageReceivedListener;
import Quick.Protocol.Utils.LogUtils;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws IOException {
		Quick.Protocol.Utils.LogUtils.LogConnection = true;
		Quick.Protocol.Utils.LogUtils.LogCommand = true;
		Quick.Protocol.Utils.LogUtils.LogPackage = true;
		Quick.Protocol.Utils.LogUtils.LogHeartbeat = true;
		Quick.Protocol.Utils.LogUtils.LogNotice = true;
		Quick.Protocol.Utils.LogUtils.LogSplit = true;
		Quick.Protocol.Utils.LogUtils.LogContent = true;
		Quick.Protocol.Utils.LogUtils.SetConsoleLogHandler();

		Quick.Protocol.Pipeline.QpPipelineClientOptions options = new Quick.Protocol.Pipeline.QpPipelineClientOptions();
		options.PipeName = "Quick.Protocol";
		options.Password = "HelloQP";
		options.EnableCompress = true;
		options.EnableEncrypt = true;

		Quick.Protocol.Pipeline.QpPipelineClient client = new Quick.Protocol.Pipeline.QpPipelineClient(options);

		client.AddRawNoticePackageReceivedListener(new RawNoticePackageReceivedListener() {

			public void Invoke(String typeName, String content) {
				LogUtils.Log("[Client_RawNoticePackageReceived]TypeName:%s,Content:%s", typeName, content);
			}

		});
		client.AddNoticePackageReceivedListener(new NoticePackageReceivedListener() {

			public void Invoke(String typeName, Object contentModel) {
				LogUtils.Log("[Client_NoticePackageReceived]TypeName:%s,ContentModel:%s", typeName, contentModel);
			}

		});

		client.addDisconnectedListener(new DisconnectedListener() {

			public void Invoke() {
				LogUtils.Log("连接已断开");
			}

		});
		try {
			client.ConnectAsync();
			LogUtils.Log("连接成功");
		} catch (Exception ex) {
			ex.printStackTrace();
			LogUtils.Log("连接出错，原因：" + ex.getMessage());
			return;
		}
	}
}
