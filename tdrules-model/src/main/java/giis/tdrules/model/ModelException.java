package giis.tdrules.model;

public class ModelException extends RuntimeException {
	private static final long serialVersionUID = -8841544107175357009L;

	public ModelException(String message) {
        super(message);
    }
	public ModelException(String message, Exception e) {
        super(message, e);
    }
}
