package giis.tdrules.store.rdb;

public class SchemaCheckConstraint {
	private String name;
	private String column;
	private String constraint;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	public String getConstraint() {
		return constraint;
	}
	public void setConstraint(String constraint) {
		this.constraint = constraint;
	}
}
