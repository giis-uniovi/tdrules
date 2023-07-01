package giis.tdrules.store.rdb;

public class SchemaException extends RuntimeException {
	private static final long serialVersionUID = -4155612383247919170L;

	public SchemaException(Throwable e) {
		super("Schema Exception", e);
	}

	public SchemaException(String message) {
		super(message);
	}

	public SchemaException(String message, Throwable cause) {
		super(message + (cause == null ? "" : ". Caused by: " + cause.toString()), cause);
	}
}
