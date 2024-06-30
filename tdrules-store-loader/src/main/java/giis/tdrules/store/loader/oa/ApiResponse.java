package giis.tdrules.store.loader.oa;

public class ApiResponse {
	private int status;
	private String reason;
	private String body;

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
