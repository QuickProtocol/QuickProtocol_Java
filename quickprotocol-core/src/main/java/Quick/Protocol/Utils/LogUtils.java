package Quick.Protocol.Utils;

public class LogUtils {
	public final static String NOT_SHOW_CONTENT_MESSAGE = "[NOT_SHOW: LogUtils.LogContent is False]";

	public interface LogHandler {
		public void Invoke(String log);
	}

	/// <summary>
	/// 是否记录心跳相关日志
	/// </summary>
	public static boolean LogPackage = false;
	public static boolean LogHeartbeat = false;
	public static boolean LogNotice = false;
	public static boolean LogCommand = false;
	public static boolean LogContent = false;
	public static boolean LogSplit = false;
	public static boolean LogConnection = false;

	private static LogHandler LogHandler = null;

	public static void SetConsoleLogHandler() {
		LogHandler = new LogHandler() {
			public void Invoke(String log) {
				System.out.println(log);
			}
		};
	}

	public static void SetLogHandler(LogHandler logHandler) {
		LogHandler = logHandler;
	}

	public static void Log(String template, Object... args) {
		Log(String.format(template, args));
	}

	public static void Log(String content) {
		if (LogHandler == null)
			return;
		LogHandler.Invoke(content);
	}
}
