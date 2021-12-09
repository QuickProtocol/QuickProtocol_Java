package Quick.Protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.codec.binary.StringUtils;

import Quick.Protocol.Listeners.HeartbeatPackageReceivedListener;
import Quick.Protocol.Listeners.NoticePackageReceivedListener;
import Quick.Protocol.Listeners.RawNoticePackageReceivedListener;
import Quick.Protocol.Utils.BitConverter;
import Quick.Protocol.Utils.ByteUtils;
import Quick.Protocol.Utils.Convert;
import Quick.Protocol.Utils.CryptographyUtils;
import Quick.Protocol.Utils.ExceptionUtils;
import Quick.Protocol.Utils.JsonConvert;
import Quick.Protocol.Utils.LogUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class QpChannel {
	static {
		// 增加PKCS7填充的支持
		java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}

	/**
	 * 包长度字节长度
	 */
	public final static int PACKAGE_TOTAL_LENGTH_LENGTH = 4;
	/**
	 * 包头长度
	 */
	public final static int PACKAGE_HEAD_LENGTH = 5;
	/**
	 * 命令编号长度(字节数)
	 */
	public final static int COMMAND_ID_LENGTH = 16;

	// 心跳包
	private static byte[] HEARTBEAT_PACKAGHE = new byte[] { 0, 0, 0, 5, 0 };
	private static ArraySegment nullArraySegment = new ArraySegment(new byte[0], 0, 0);

	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	// 接收缓存
	private byte[] recvBuffer;
	private byte[] recvBuffer2;
	// 发送缓存
	private byte[] sendBuffer;
	private byte[] sendBuffer2;

	private InputStream QpPackageHandler_InputStream;
	private OutputStream QpPackageHandler_OutputStream;

	private QpChannelOptions options;
	private Date lastSendPackageTime = new Date(0);

	private byte[] passwordMd5Buffer;
	private Cipher enc;
	private Cipher dec;

	private HashMap<String, Class> commandRequestTypeDict = new HashMap<String, Class>();
	private HashMap<String, Class> commandResponseTypeDict = new HashMap<String, Class>();
	private HashMap<Class, Class> commandRequestTypeResponseTypeDict = new HashMap<Class, Class>();

	// private ConcurrentDictionary<string, CommandContext> commandDict = new
	// ConcurrentDictionary<string, CommandContext>();

	/**
	 * 当前是否连接
	 */
	public Boolean IsConnected;
	/**
	 * 最后的异常
	 */
	public Exception LastException;

	/**
	 * 缓存大小，初始大小为1KB
	 */
	protected int BufferSize = 1 * 1024;

	private interface PackagePayloadGenerator {
		public ArraySegment Invoke(byte[] buffer);
	}

	protected void ChangeBufferSize(int bufferSize) {
		// 缓存大小最小为1KB
		if (bufferSize < 1 * 1024)
			bufferSize = 1 * 1024;
		BufferSize = bufferSize;
		recvBuffer = new byte[bufferSize];
		recvBuffer2 = new byte[bufferSize];
		sendBuffer = new byte[bufferSize];
		sendBuffer2 = new byte[bufferSize];
	}

	protected void ChangeTransportTimeout() {
		/*
		 * OutputStream stream = QpPackageHandler_OutputStream; if (stream != null &&
		 * stream.CanTimeout) { stream.WriteTimeout = options.InternalTransportTimeout;
		 * stream.ReadTimeout = options.InternalTransportTimeout; }
		 */
	}

	/// <summary>
	/// 增加Tag属性，用于引用与处理器相关的对象
	/// </summary>
	public Object Tag;

	private HashMap<String, Class> noticeTypeDict = new HashMap<String, Class>();

	public QpChannel(QpChannelOptions options) {
		this.options = options;
		ChangeBufferSize(BufferSize);

		byte[] tmpBuffer = CryptographyUtils.ComputeMD5Hash(StringUtils.getBytesUtf8(options.Password));
		passwordMd5Buffer = Arrays.copyOf(tmpBuffer, 8);

		try {
			DESKeySpec dks = new DESKeySpec(passwordMd5Buffer);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = keyFactory.generateSecret(dks);

			String CIPHER_ALGORITHM = "DES/ECB/PKCS7PADDING";
			dec = Cipher.getInstance(CIPHER_ALGORITHM);
			dec.init(Cipher.DECRYPT_MODE, secretKey);

			enc = Cipher.getInstance(CIPHER_ALGORITHM);
			enc.init(Cipher.ENCRYPT_MODE, secretKey);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		for (QpInstruction instructionSet : options.GetInstructionSet()) {
			// 添加通知数据包信息
			if (instructionSet.NoticeInfos != null && instructionSet.NoticeInfos.length > 0) {
				for (QpNoticeInfo item : instructionSet.NoticeInfos) {
					noticeTypeDict.put(item.NoticeTypeName, item.GetNoticeType());
				}
				// 添加命令数据包信息
				if (instructionSet.CommandInfos != null && instructionSet.CommandInfos.length > 0) {
					for (QpCommandInfo item : instructionSet.CommandInfos) {
						Class requestType = item.GetRequestType();
						Class responseType = item.GetResponseType();
						commandRequestTypeDict.put(item.RequestTypeName, requestType);
						commandResponseTypeDict.put(item.ResponseTypeName, responseType);
						commandRequestTypeResponseTypeDict.put(requestType, responseType);
					}
				}
			}
		}
	}

	protected void InitQpPackageHandler_Stream(InputStream instream, OutputStream outstream) {
		if (instream != null) {
			try {
				QpPackageHandler_InputStream.close();
			} catch (Exception ex) {
			}
		}
		QpPackageHandler_InputStream = instream;
		if (outstream != null) {
			try {
				QpPackageHandler_OutputStream.close();
			} catch (Exception ex) {
			}
		}
		QpPackageHandler_OutputStream = outstream;
		options.InternalCompress = false;
		options.InternalEncrypt = false;
		ChangeTransportTimeout();
	}

	/**
	 * 当读取出错时
	 * 
	 * @param exception 异常
	 */
	protected void OnReadError(Exception exception) {
		LastException = exception;
		LogUtils.Log("[ReadError]%s: %s", dateFormat.format(new Date()), ExceptionUtils.GetExceptionString(exception));
		InitQpPackageHandler_Stream(null, null);
	}

	private void writePackageTotalLengthToBuffer(byte[] buffer, int offset, int packageTotalLength) {
		// 构造包头
		byte[] ret = BitConverter.GetBytes(packageTotalLength);
		// 如果是小端字节序，则交换
		if (BitConverter.IsLittleEndian) {
			Collections.reverse(Arrays.asList(ret));
		}
		System.arraycopy(ret, 0, buffer, offset, 4);
	}

	// 获取空闲的缓存
	private byte[] getFreeBuffer(byte[] usingBuffer, byte[]... bufferArray) {
		for (byte[] buffer : bufferArray) {
			if (usingBuffer != buffer)
				return buffer;
		}
		return null;
	}

	private void sendPackageBuffer(OutputStream stream, ArraySegment packageBuffer, Runnable afterSendHandler) {
		byte packageType = packageBuffer.getArray()[packageBuffer.getOffset() + PACKAGE_HEAD_LENGTH - 1];
		try {
			// 如果压缩或者加密
			if (options.InternalCompress || options.InternalEncrypt) {
				// 如果压缩
				if (options.InternalCompress) {
					byte[] currentBuffer = getFreeBuffer(packageBuffer.getArray(), sendBuffer, sendBuffer2);
					ByteArrayOutputStream ms = new ByteArrayOutputStream(currentBuffer.length);

					// 写入包长度
					for (int i = 0; i < PACKAGE_TOTAL_LENGTH_LENGTH; i++)
						ms.write(0);
					GZIPOutputStream gzStream = new GZIPOutputStream(ms);

					gzStream.write(packageBuffer.getArray(), packageBuffer.getOffset() + PACKAGE_TOTAL_LENGTH_LENGTH,
							packageBuffer.getCount() - PACKAGE_TOTAL_LENGTH_LENGTH);
					gzStream.flush();
					gzStream.close();

					int packageTotalLength = ms.size();
					ms.writeBytes(currentBuffer);
					ms.close();

					writePackageTotalLengthToBuffer(currentBuffer, 0, packageTotalLength);
					packageBuffer = new ArraySegment(currentBuffer, 0, packageTotalLength);
				}
				// 如果加密
				if (options.InternalEncrypt) {
					byte[] retBuffer = enc.doFinal(packageBuffer.getArray(),
							packageBuffer.getOffset() + PACKAGE_TOTAL_LENGTH_LENGTH,
							packageBuffer.getCount() - PACKAGE_TOTAL_LENGTH_LENGTH);
					int packageTotalLength = PACKAGE_TOTAL_LENGTH_LENGTH + retBuffer.length;
					byte[] currentBuffer = getFreeBuffer(packageBuffer.getArray(), sendBuffer, sendBuffer2);
					// 写入包长度
					writePackageTotalLengthToBuffer(currentBuffer, 0, packageTotalLength);
					System.arraycopy(retBuffer, 0, currentBuffer, PACKAGE_TOTAL_LENGTH_LENGTH, retBuffer.length);
					packageBuffer = new ArraySegment(currentBuffer, 0, packageTotalLength);
				}
			}
			// 执行AfterSendHandler
			if (afterSendHandler != null)
				afterSendHandler.run();
			// 发送包内容
			stream.write(packageBuffer.getArray(), packageBuffer.getOffset(), packageBuffer.getCount());
			if (LogUtils.LogPackage)
				LogUtils.Log("%s: [Send-Package]Length:%s，Type:%s，Content:%s", dateFormat.format(new Date()),
						packageBuffer.getCount(), packageType,
						LogUtils.LogContent
								? BitConverter.ToString(packageBuffer.getArray(), packageBuffer.getOffset(),
										packageBuffer.getCount())
								: LogUtils.NOT_SHOW_CONTENT_MESSAGE);
			stream.flush();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private synchronized void sendPackage(PackagePayloadGenerator getPackagePayloadFunc, Runnable afterSendHandler) {
		OutputStream stream = QpPackageHandler_OutputStream;
		if (stream == null)
			return;

		ArraySegment ret = getPackagePayloadFunc.Invoke(sendBuffer);
		int packageTotalLength = ret.getCount();
		if (packageTotalLength < PACKAGE_HEAD_LENGTH)
			throw new RuntimeException(String.format("包大小[%s]小于包头长度[%s]", packageTotalLength, PACKAGE_HEAD_LENGTH));

		byte[] packageBuffer = ret.getArray();

		// 构造包头
		writePackageTotalLengthToBuffer(packageBuffer, 0, packageTotalLength);
		try {
			// 如果包缓存是发送缓存
			if (packageBuffer == sendBuffer) {
				sendPackageBuffer(stream, new ArraySegment(packageBuffer, 0, packageTotalLength), afterSendHandler);
			}
			// 否则，拆分为多个包发送
			else {
				if (LogUtils.LogSplit)
					LogUtils.Log("%s: [Send-SplitPackage]Length:%s", dateFormat.format(new Date()), packageTotalLength);

				// 每个包内容的最大长度为对方缓存大小减包头大小
				int maxTakeLength = BufferSize - PACKAGE_HEAD_LENGTH;
				int currentIndex = 0;
				while (currentIndex < packageTotalLength) {
					int restLength = packageTotalLength - currentIndex;
					int takeLength = 0;
					if (restLength >= maxTakeLength)
						takeLength = maxTakeLength;
					else
						takeLength = restLength;
					// 构造包头
					writePackageTotalLengthToBuffer(sendBuffer, 0, PACKAGE_HEAD_LENGTH + takeLength);
					sendBuffer[4] = QpPackageType.Split;
					// 复制包体
					System.arraycopy(packageBuffer, currentIndex, sendBuffer, PACKAGE_HEAD_LENGTH, takeLength);
					// 发送
					sendPackageBuffer(stream, new ArraySegment(sendBuffer, 0, PACKAGE_HEAD_LENGTH + takeLength),
							afterSendHandler);
					currentIndex += takeLength;
				}
			}
			lastSendPackageTime = new Date();
		} catch (Exception ex) {
			LastException = ex;
			LogUtils.Log("[SendPackage]" + ExceptionUtils.GetExceptionString(ex));
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 发送心跳包
	 */
	public void SendHeartbeatPackage() {
		sendPackage(new PackagePayloadGenerator() {
			public ArraySegment Invoke(byte[] buffer) {
				System.arraycopy(HEARTBEAT_PACKAGHE, 0, buffer, 0, HEARTBEAT_PACKAGHE.length);
				return new ArraySegment(buffer, 0, HEARTBEAT_PACKAGHE.length);
			}
		}, null);
	}

	/**
	 * 发送通知包
	 * 
	 * @param noticePackageTypeName
	 * @param noticePackageContent
	 */
	public void SendNoticePackage(final String noticePackageTypeName, final String noticePackageContent) {
		sendPackage(new PackagePayloadGenerator() {
			public ArraySegment Invoke(byte[] buffer) {
				// 设置包类型
				buffer[PACKAGE_HEAD_LENGTH - 1] = (byte) QpPackageType.Notice;
				String typeName = noticePackageTypeName;
				String content = noticePackageContent;

				int typeNameByteLengthOffset = PACKAGE_HEAD_LENGTH;
				// 写入类名
				int typeNameByteOffset = typeNameByteLengthOffset + 1;
				byte[] typeNameByteArray = StringUtils.getBytesUtf8(typeName);
				System.arraycopy(typeNameByteArray, 0, buffer, typeNameByteOffset, typeNameByteArray.length);
				int typeNameByteLength = typeNameByteArray.length;
				// 写入类名长度
				buffer[typeNameByteLengthOffset] = Convert.ToByte(typeNameByteLength);

				int contentOffset = typeNameByteOffset + typeNameByteLength;
				byte[] contentByteArray = StringUtils.getBytesUtf8(content);
				int contentLength = contentByteArray.length;

				byte[] retBuffer = buffer;
				// 如果内容超出了缓存可用空间的大小
				if (contentLength > buffer.length - contentOffset) {
					retBuffer = new byte[contentOffset + contentLength];
					System.arraycopy(buffer, 0, retBuffer, 0, contentOffset);
				}
				System.arraycopy(contentByteArray, 0, retBuffer, contentOffset, contentLength);

				// 包总长度
				int packageTotalLength = contentOffset + contentLength;

				if (LogUtils.LogNotice)
					LogUtils.Log("%s: [Send-NoticePackage]Type:%s,Content:%s", dateFormat.format(new Date()), typeName,
							LogUtils.LogContent ? content : LogUtils.NOT_SHOW_CONTENT_MESSAGE);

				return new ArraySegment(retBuffer, 0, packageTotalLength);
			}
		}, null);
	}

	/**
	 * 发送通知包
	 * 
	 * @param packageObj
	 */
	public void SendNoticePackage(Object packageObj) {
		SendNoticePackage(packageObj.getClass().getName(), JsonConvert.SerializeObject(packageObj));
	}

	/**
	 * 发送命令请求包
	 * 
	 * @param commandId
	 * @param typeName
	 * @param content
	 * @param afterSendHandler
	 */
	public void SendCommandRequestPackage(final String commandId, final String typeName, final String content,
			Runnable afterSendHandler) {
		sendPackage(new PackagePayloadGenerator() {
			public ArraySegment Invoke(byte[] buffer) {
				// 设置包类型
				buffer[PACKAGE_HEAD_LENGTH - 1] = (byte) QpPackageType.CommandRequest;
				// 写入指令编号
				int commandIdBufferOffset = PACKAGE_HEAD_LENGTH;
				byte[] commandIdBuffer = BitConverter.GetBytes(UUID.fromString(commandId));
				System.arraycopy(commandIdBuffer, 0, buffer, commandIdBufferOffset, commandIdBuffer.length);

				int typeNameByteLengthOffset = commandIdBufferOffset + 16;
				// 写入类名
				int typeNameByteOffset = typeNameByteLengthOffset + 1;
				byte[] typeNameByteArray = StringUtils.getBytesUtf8(typeName);
				int typeNameByteLength = typeNameByteArray.length;
				System.arraycopy(typeNameByteArray, 0, buffer, typeNameByteOffset, typeNameByteLength);
				// 写入类名长度
				buffer[typeNameByteLengthOffset] = Convert.ToByte(typeNameByteLength);

				int contentOffset = typeNameByteOffset + typeNameByteLength;
				byte[] contentByteArray = StringUtils.getBytesUtf8(content);
				int contentLength = contentByteArray.length;

				byte[] retBuffer = buffer;
				// 如果内容超出了缓存可用空间的大小
				if (contentLength > buffer.length - contentOffset) {
					retBuffer = new byte[contentOffset + contentLength];
					System.arraycopy(buffer, 0, retBuffer, 0, contentOffset);
				}
				System.arraycopy(contentByteArray, 0, retBuffer, contentOffset, contentLength);

				// 包总长度
				int packageTotalLength = contentOffset + contentLength;

				if (LogUtils.LogCommand)
					LogUtils.Log("%s: [Send-CommandRequestPackage]CommandId:%s,Type:%s,Content:%s",
							dateFormat.format(new Date()), commandId, typeName,
							LogUtils.LogContent ? content : LogUtils.NOT_SHOW_CONTENT_MESSAGE);

				return new ArraySegment(retBuffer, 0, packageTotalLength);
			}
		}, afterSendHandler);
	}

	/**
	 * 发送命令响应包
	 * 
	 * @param commandId
	 * @param code
	 * @param message
	 * @param typeName
	 * @param content
	 */
	public void SendCommandResponsePackage(final String commandId, final byte code, final String message,
			final String typeName, final String content) {
		sendPackage(new PackagePayloadGenerator() {
			public ArraySegment Invoke(byte[] buffer) {
				// 设置包类型
				buffer[PACKAGE_HEAD_LENGTH - 1] = (byte) QpPackageType.CommandResponse;

				// 写入指令编号
				int commandIdBufferOffset = PACKAGE_HEAD_LENGTH;
				byte[] commandIdBuffer = BitConverter.GetBytes(UUID.fromString(commandId));
				System.arraycopy(commandIdBuffer, 0, buffer, commandIdBufferOffset, commandIdBuffer.length);

				// 写入返回码
				int codeByteOffset = commandIdBufferOffset + commandIdBuffer.length;
				buffer[codeByteOffset] = code;

				// 如果是成功
				if (code == 0) {
					int typeNameByteLengthOffset = codeByteOffset + 1;
					// 写入类名
					int typeNameByteOffset = typeNameByteLengthOffset + 1;
					byte[] typeNameByteArray = StringUtils.getBytesUtf8(typeName);
					int typeNameByteLength = typeNameByteArray.length;
					System.arraycopy(typeNameByteArray, 0, buffer, typeNameByteOffset, typeNameByteLength);

					// 写入类名长度
					buffer[typeNameByteLengthOffset] = Convert.ToByte(typeNameByteLength);

					int contentOffset = typeNameByteOffset + typeNameByteLength;
					byte[] contentByteArray = StringUtils.getBytesUtf8(content);
					int contentLength = contentByteArray.length;

					byte[] retBuffer = buffer;
					// 如果内容超出了缓存可用空间的大小
					if (contentLength > buffer.length - contentOffset) {
						retBuffer = new byte[contentOffset + contentLength];
						System.arraycopy(buffer, 0, retBuffer, 0, contentOffset);
					}
					System.arraycopy(contentByteArray, 0, retBuffer, contentOffset, contentLength);

					// 包总长度
					int packageTotalLength = contentOffset + contentLength;

					if (LogUtils.LogCommand)
						LogUtils.Log("%s: [Send-CommandResponsePackage]CommandId:%s,Code:%s,Type:%s,Content:%s",
								dateFormat.format(new Date()), commandId, code, typeName,
								LogUtils.LogContent ? content : LogUtils.NOT_SHOW_CONTENT_MESSAGE);

					return new ArraySegment(retBuffer, 0, packageTotalLength);
				}
				// 如果是失败
				else {
					int messageOffset = codeByteOffset + 1;
					byte[] messageByteArray = StringUtils.getBytesUtf8(message);
					int messageLength = messageByteArray.length;

					// 如果内容超出了缓存可用空间的大小
					byte[] retBuffer = buffer;
					if (messageLength > buffer.length - messageOffset) {
						retBuffer = new byte[messageOffset + messageLength];
						System.arraycopy(buffer, 0, retBuffer, 0, messageOffset);
					}
					System.arraycopy(messageByteArray, 0, retBuffer, messageOffset, messageLength);

					// 包总长度
					int packageTotalLength = messageOffset + messageLength;

					if (LogUtils.LogNotice)
						LogUtils.Log("%s: [Send-CommandResponsePackage]CommandId:%s,Code:%s,Message:%s",
								dateFormat.format(new Date()), commandId, code, message);

					return new ArraySegment(retBuffer, 0, packageTotalLength);
				}
			}
		}, null);
	}

	private int readData(InputStream stream, byte[] buffer, int startIndex, int totalCount,
			CancellationToken cancellationToken) {
		try {
			if (totalCount > buffer.length - startIndex)
				throw new IOException(
						String.format("要接收的数据大小[%s]超出了缓存的大小[%s]！", totalCount, buffer.length - startIndex));
			int ret;
			int count = 0;

			Date beginWaitTime = new Date();
			while (count < totalCount) {
				if (cancellationToken.IsCancellationRequested())
					break;

				int streamAvailable = stream.available();
				if (streamAvailable < 0)
					throw new RuntimeException("从网络流中读取错误！");
				if (streamAvailable == 0) {
					Date currentTime = new Date();
					long usedTime = currentTime.getTime() - beginWaitTime.getTime();
					if (usedTime > options.InternalTransportTimeout)
						throw new RuntimeException("读取超时", new TimeoutException());
					Thread.sleep(100);
					continue;
				}
				beginWaitTime = new Date();
				ret = stream.read(buffer, count + startIndex, Math.min(totalCount - count, streamAvailable));

				if (cancellationToken.IsCancellationRequested() || ret == 0)
					break;
				if (ret < 0)
					throw new IOException("从网络流中读取错误！");
				count += ret;
			}
			return count;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 读取一个数据包
	 * 
	 * @param token
	 * @return
	 */
	protected ArraySegment ReadPackageAsync(CancellationToken token) {

		try {
			InputStream stream = QpPackageHandler_InputStream;
			if (stream == null)
				throw new IOException("QpPackageHandler_InputStream is null.");

			// 最终包缓存
			ArraySegment finalPackageBuffer;

			// 是否正在读取拆分包
			boolean isReadingSplitPackage = false;
			int splitMsCapacity = 0;
			ByteArrayOutputStream splitMs = null;
			while (true) {
				byte[] currentRecvBuffer = recvBuffer;
				// 读取包头
				int ret = readData(stream, currentRecvBuffer, 0, PACKAGE_TOTAL_LENGTH_LENGTH, token);
				if (token.IsCancellationRequested())
					return nullArraySegment;
				if (ret == 0)
					throw new IOException("未读取到数据！");
				if (ret < PACKAGE_TOTAL_LENGTH_LENGTH)
					throw new ProtocolException(new ArraySegment(recvBuffer, 0, ret),
							String.format("包头读取错误！包头长度：%s，读取数据长度：%s", PACKAGE_TOTAL_LENGTH_LENGTH, ret));

				byte[] currentRecvBuffer2 = recvBuffer;

				// 如果读取缓存数组对象变化
				if (currentRecvBuffer != currentRecvBuffer2)
					System.arraycopy(currentRecvBuffer, 0, currentRecvBuffer2, 0, PACKAGE_TOTAL_LENGTH_LENGTH);

				// 包总长度
				int packageTotalLength = ByteUtils.B2I_BE(recvBuffer, 0);

				if (packageTotalLength < PACKAGE_HEAD_LENGTH)
					throw new ProtocolException(new ArraySegment(recvBuffer, 0, ret),
							String.format("包长度[%s]必须大于等于%s！", packageTotalLength, PACKAGE_HEAD_LENGTH));
				if (packageTotalLength > recvBuffer.length)
					throw new ProtocolException(new ArraySegment(recvBuffer, 0, ret),
							String.format("数据包总长度[%s]大于缓存大小[%s]", packageTotalLength, recvBuffer.length));
				// 包体长度
				int packageBodyLength = packageTotalLength - PACKAGE_TOTAL_LENGTH_LENGTH;
				// 读取包体
				ret = readData(stream, recvBuffer, PACKAGE_TOTAL_LENGTH_LENGTH, packageBodyLength, token);

				if (token.IsCancellationRequested())
					return nullArraySegment;
				if (ret < packageBodyLength)
					throw new ProtocolException(new ArraySegment(recvBuffer, 0, PACKAGE_HEAD_LENGTH + ret),
							String.format("包体读取错误！包体长度：%s，读取数据长度：%s", packageBodyLength, ret));

				ArraySegment currentPackageBuffer = new ArraySegment(recvBuffer, 0, packageTotalLength);

				// 如果设置了压缩或者加密
				if (options.InternalCompress || options.InternalEncrypt) {
					// 如果设置了加密，则先解密
					if (options.InternalEncrypt) {
						byte[] retBuffer = dec.doFinal(currentPackageBuffer.getArray(),
								PACKAGE_TOTAL_LENGTH_LENGTH + currentPackageBuffer.getOffset(),
								currentPackageBuffer.getCount() - PACKAGE_TOTAL_LENGTH_LENGTH);
						byte[] currentBuffer = getFreeBuffer(currentPackageBuffer.getArray(), recvBuffer, recvBuffer2);
						packageTotalLength = PACKAGE_TOTAL_LENGTH_LENGTH + retBuffer.length;
						writePackageTotalLengthToBuffer(currentBuffer, 0, packageTotalLength);
						System.arraycopy(retBuffer, 0, currentBuffer, PACKAGE_TOTAL_LENGTH_LENGTH, retBuffer.length);
						currentPackageBuffer = new ArraySegment(currentBuffer, 0, packageTotalLength);
					}
					// 如果设置了压缩，则先解压
					if (options.InternalCompress) {
						byte[] retBuffer = getFreeBuffer(currentPackageBuffer.getArray(), recvBuffer, recvBuffer2);
						int count = 0;

						ByteArrayInputStream readMs = new ByteArrayInputStream(currentPackageBuffer.getArray(),
								PACKAGE_TOTAL_LENGTH_LENGTH + currentPackageBuffer.getOffset(),
								currentPackageBuffer.getCount() - PACKAGE_TOTAL_LENGTH_LENGTH);
						GZIPInputStream gzStream = new GZIPInputStream(readMs);
						MemoryOutputStream writeMs = new MemoryOutputStream(retBuffer, 0, retBuffer.length);
						count = (int) gzStream.transferTo(writeMs);

						byte[] currentBuffer = getFreeBuffer(retBuffer, recvBuffer, recvBuffer2);
						packageTotalLength = PACKAGE_TOTAL_LENGTH_LENGTH + count;
						writePackageTotalLengthToBuffer(currentBuffer, 0, packageTotalLength);
						System.arraycopy(retBuffer, 0, currentBuffer, PACKAGE_TOTAL_LENGTH_LENGTH, count);
						currentPackageBuffer = new ArraySegment(currentBuffer, 0, packageTotalLength);
					}
				}
				int packageType = currentPackageBuffer.getArray()[currentPackageBuffer.getOffset() + PACKAGE_HEAD_LENGTH
						- 1];
				// 如果当前包是拆分包
				if (packageType == QpPackageType.Split) {
					if (!isReadingSplitPackage) {
						int tmpPackageBodyLength = ByteUtils.B2I_BE(currentPackageBuffer.getArray(),
								currentPackageBuffer.getOffset() + PACKAGE_HEAD_LENGTH);
						splitMsCapacity = tmpPackageBodyLength;
						if (splitMsCapacity <= 0)
							throw new IOException(String.format("拆分包中包长度[%s]必须为正数！", splitMsCapacity));
						if (splitMsCapacity > options.MaxPackageSize)
							throw new IOException(
									String.format("拆分包中包长度[%s]大于最大包大小[{%}]", splitMsCapacity, options.MaxPackageSize));
						splitMs = new ByteArrayOutputStream(splitMsCapacity);
						isReadingSplitPackage = true;
					}
					splitMs.write(currentPackageBuffer.getArray(),
							currentPackageBuffer.getOffset() + PACKAGE_HEAD_LENGTH,
							currentPackageBuffer.getCount() - PACKAGE_HEAD_LENGTH);
					// 如果拆分包已经读取完成
					if (splitMs.size() >= splitMsCapacity) {
						byte[] splitMsArray = splitMs.toByteArray();
						finalPackageBuffer = new ArraySegment(splitMsArray, 0, splitMsArray.length);
						splitMs.close();
						if (LogUtils.LogSplit)
							LogUtils.Log("%s: [Recv-SplitPackage]Length:%s", dateFormat.format(new Date()),
									finalPackageBuffer.getCount());
						break;
					}
				} else {
					finalPackageBuffer = currentPackageBuffer;
					break;
				}
			}

			if (LogUtils.LogPackage)
				LogUtils.Log("%s: [Recv-Package]Length:%s，Type:%s，Content:%s", dateFormat.format(new Date()),
						finalPackageBuffer.getCount(),
						finalPackageBuffer.getArray()[finalPackageBuffer.getOffset() + PACKAGE_HEAD_LENGTH - 1],
						LogUtils.LogContent ? BitConverter.ToString(finalPackageBuffer.getArray(),
								finalPackageBuffer.getOffset(), finalPackageBuffer.getCount())
								: LogUtils.NOT_SHOW_CONTENT_MESSAGE);

			return new ArraySegment(finalPackageBuffer.getArray(), finalPackageBuffer.getOffset(),
					finalPackageBuffer.getCount());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	protected void BeginHeartBeat(final CancellationToken cancellationToken) {
		if (QpPackageHandler_OutputStream == null)
			return;

		if (options.getHeartBeatInterval() > 0) {
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				public void run() {
					if (cancellationToken.IsCancellationRequested()) {
						timer.cancel();
						return;
					}
					if (QpPackageHandler_OutputStream == null)
						return;

					long lastSendPackageToNowSeconds = new Date().getTime() - lastSendPackageTime.getTime();
					// 如果离最后一次发送数据包的时间大于心跳间隔，则发送心跳包
					if (lastSendPackageToNowSeconds > options.getHeartBeatInterval()) {
						SendHeartbeatPackage();
					}
				}
			}, 0, options.getHeartBeatInterval());
		}
	}

	private ArrayList<HeartbeatPackageReceivedListener> HeartbeatPackageReceivedListeners = new ArrayList<HeartbeatPackageReceivedListener>();

	/**
	 * 添加收到心跳数据包事件
	 * 
	 * @param listener
	 */
	public void AddHeartbeatPackageReceivedListener(HeartbeatPackageReceivedListener listener) {
		HeartbeatPackageReceivedListeners.add(listener);
	}

	/**
	 * 移除收到心跳数据包事件
	 * 
	 * @param listener
	 */
	public void RemoveHeartbeatPackageReceivedListener(HeartbeatPackageReceivedListener listener) {
		HeartbeatPackageReceivedListeners.remove(listener);
	}

	private ArrayList<RawNoticePackageReceivedListener> RawNoticePackageReceivedListeners = new ArrayList<RawNoticePackageReceivedListener>();

	/**
	 * 添加收到原始通知数据包事件
	 * 
	 * @param listener
	 */
	public void AddRawNoticePackageReceivedListener(RawNoticePackageReceivedListener listener) {
		RawNoticePackageReceivedListeners.add(listener);
	}

	/**
	 * 移除收到原始通知数据包事件
	 * 
	 * @param listener
	 */
	public void RemoveRawNoticePackageReceivedListener(RawNoticePackageReceivedListener listener) {
		RawNoticePackageReceivedListeners.remove(listener);
	}

	private ArrayList<NoticePackageReceivedListener> NoticePackageReceivedListeners = new ArrayList<NoticePackageReceivedListener>();

	/**
	 * 添加收到通知数据包事件
	 * 
	 * @param listener
	 */
	public void AddNoticePackageReceivedListener(NoticePackageReceivedListener listener) {
		NoticePackageReceivedListeners.add(listener);
	}

	/**
	 * 移除收到通知数据包事件
	 * 
	 * @param listener
	 */
	public void RemoveNoticePackageReceivedListener(NoticePackageReceivedListener listener) {
		NoticePackageReceivedListeners.remove(listener);
	}

	/**
	 * 接收到原始通知数据包时
	 * 
	 * @param typeName
	 * @param content
	 */
	protected void OnRawNoticePackageReceived(String typeName, String content) {
		// 触发RawNoticePackageReceived事件
		if (RawNoticePackageReceivedListeners.size() > 0) {
			for (RawNoticePackageReceivedListener listener : RawNoticePackageReceivedListeners) {
				listener.Invoke(typeName, content);
			}
		}

		// 如果配置了触发NoticePackageReceived事件
		if (options.RaiseNoticePackageReceivedEvent) {
			// 如果在字典中未找到此类型名称，则直接返回
			if (!noticeTypeDict.containsKey(typeName))
				return;

			if (NoticePackageReceivedListeners.size() > 0) {
				Object contentModel = JsonConvert.DeserializeObject(content, noticeTypeDict.get(typeName));
				for (NoticePackageReceivedListener listener : NoticePackageReceivedListeners) {
					listener.Invoke(typeName, contentModel);
				}
			}
		}
	}
	
	
}