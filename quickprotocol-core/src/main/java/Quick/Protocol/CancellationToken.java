package Quick.Protocol;

public class CancellationToken {
	private boolean isCancellationRequested = false;

	public void Cancel() {
		isCancellationRequested = true;
	}

	public boolean IsCancellationRequested() {
		return isCancellationRequested;
	}
}
