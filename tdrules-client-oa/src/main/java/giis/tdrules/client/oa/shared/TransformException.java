package giis.tdrules.client.oa.shared;

public class TransformException extends RuntimeException {
	private static final long serialVersionUID = -3395114979531982805L;

	public TransformException(Throwable e) {
        super(e);
    }
    public TransformException(String message) {
        super(message);
    }
    public TransformException(String message, Throwable cause) {
        super(message + (cause== null ? "" : ". Caused by: " + cause.toString()), cause);
    }
}
