package giis.tdrules.store.loader.oa;

public class ApiResponse {
	private final int status;
	private final String reason;
	private final String body;

	public ApiResponse(int status, String reason, String body) {
		this.status = status;
		this.reason = reason;
		this.body = body;
	}

	public int getStatus() {
		return status;
	}

	public String getReason() {
		return reason;
	}

	public String getBody() {
		return body;
	}

}
