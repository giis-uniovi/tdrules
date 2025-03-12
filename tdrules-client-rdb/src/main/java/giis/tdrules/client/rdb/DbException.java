package giis.tdrules.client.rdb;

public class DbException extends RuntimeException {
	private static final long serialVersionUID = 5671164449308159998L;

	public DbException(String message, Exception cause) {
		super(message + (cause == null ? "" : ". Caused by: " + cause.toString()), cause);
	}
}
