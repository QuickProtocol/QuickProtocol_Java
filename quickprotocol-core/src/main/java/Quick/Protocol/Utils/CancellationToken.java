package Quick.Protocol.Utils;

import java.util.ArrayList;

public class CancellationToken {
	private boolean isCancellationRequested = false;
	private ArrayList<Runnable> runnableList = new ArrayList<Runnable>();

	public void Cancel() {
		isCancellationRequested = true;
		for (Runnable runnable : runnableList)
			runnable.run();
	}

	public boolean IsCancellationRequested() {
		return isCancellationRequested;
	}

	public void Register(Runnable runnable) {
		runnableList.add(runnable);
	}

	public void UnRegister(Runnable runnable) {
		runnableList.remove(runnable);
	}
}
