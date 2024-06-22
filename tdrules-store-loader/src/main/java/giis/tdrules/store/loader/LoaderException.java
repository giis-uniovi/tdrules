package giis.tdrules.store.loader;

public class LoaderException extends RuntimeException {
	private static final long serialVersionUID = -7517561777592540389L;
	
	public LoaderException(Throwable e) {
        super(e);
    }
    public LoaderException(String message) {
        super(message);
    }
    public LoaderException(String message, Throwable cause) {
        super(message + (cause== null ? "" : ". Caused by: " + cause.toString()), cause);
    }
}
