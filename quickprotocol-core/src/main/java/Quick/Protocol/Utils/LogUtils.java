package Quick.Protocol.Utils;

public class LogUtils {
	public final static String NOT_SHOW_CONTENT_MESSAGE = "[NOT_SHOW: LogUtils.LogContent is False]";

	public interface LogHandler {
		public void Invoke(String log);
	}

	/// <summary>
	/// 是否记录心跳相关日志
	/// </summary>
	public static Boolean LogPackage = false;
	public static Boolean LogHeartbeat = false;
	public static Boolean LogNotice = false;
	public static Boolean LogCommand = false;
	public static Boolean LogContent = false;
	public static Boolean LogSplit = false;
	public static Boolean LogConnection = false;

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
