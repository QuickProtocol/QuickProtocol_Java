package Quick.Protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.codec.binary.StringUtils;

import Quick.Protocol.Utils.BitConverter;
import Quick.Protocol.Utils.CryptographyUtils;
import Quick.Protocol.Utils.ExceptionUtils;
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
		LogUtils.Log("[ReadError]{0}: {1}", dateFormat.format(new Date()),
				ExceptionUtils.GetExceptionString(exception));
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
					/*
					 * using (var ms = new MemoryStream(currentBuffer)) { //写入包长度 for (var i = 0; i
					 * < PACKAGE_TOTAL_LENGTH_LENGTH; i++) ms.WriteByte(0); using (var gzStream =
					 * new GZipStream(ms, CompressionMode.Compress, true))
					 * gzStream.Write(packageBuffer.Array, packageBuffer.Offset +
					 * PACKAGE_TOTAL_LENGTH_LENGTH, packageBuffer.Count -
					 * PACKAGE_TOTAL_LENGTH_LENGTH); var packageTotalLength =
					 * Convert.ToInt32(ms.Position); writePackageTotalLengthToBuffer(currentBuffer,
					 * 0, packageTotalLength); packageBuffer = new ArraySegment<byte>(currentBuffer,
					 * 0, packageTotalLength); }
					 */
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
				LogUtils.Log("{0}: [Send-Package]Length:{1}，Type:{2}，Content:{3}", dateFormat.format(new Date()),
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
			throw new RuntimeException(String.format("包大小[{0}]小于包头长度[{1}]", packageTotalLength, PACKAGE_HEAD_LENGTH));

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
					LogUtils.Log("{0}: [Send-SplitPackage]Length:{1}", dateFormat.format(new Date()),
							packageTotalLength);

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
}