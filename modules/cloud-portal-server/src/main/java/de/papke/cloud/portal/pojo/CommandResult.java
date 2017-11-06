package de.papke.cloud.portal.pojo;

public class CommandResult {
	
	private String output;
	private boolean success;

	public CommandResult() {}
	
	public CommandResult(String output, boolean success) {
		this.output = output;
		this.success = success;
	}
	
	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
}
