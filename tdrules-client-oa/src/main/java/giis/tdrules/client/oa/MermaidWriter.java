package giis.tdrules.client.oa;

import giis.tdrules.client.oa.mermaid.MermaidEntityWriter;
import giis.tdrules.client.oa.mermaid.MermaidPathWriter;
import giis.tdrules.openapi.model.TdSchema;

/**
 * Produces a Mermaid string with a graphical representation of a TdSchema
 */
public class MermaidWriter {

	private TdSchema schema;

	private enum Direction { DEFAULT, LEFT_RIGHT }
	private Direction direction = Direction.DEFAULT;
	
	private boolean linkEntitiesInPath = false;
	private boolean boxEntitiesInPath = false;
	
	public MermaidWriter(TdSchema schema) {
		this.schema = schema;
	}
	
	/**
	 * Sets the drawing direction by placing the top entities at the left of the diagram
	 */
	public MermaidWriter setLeftToRight() {
		this.direction = Direction.LEFT_RIGHT;
		return this;
	}
	
	/**
	 * Draws a link between entities that are in the same path and operation
	 */
	public MermaidWriter setLinkEntitiesInPath() {
		this.linkEntitiesInPath = true;
		return this;
	}

	/**
	 * Groups in a box the entities that are in the same path and operation
	 */
	public MermaidWriter setGroupEntitiesInPath() {
		this.boxEntitiesInPath = true;
		return this;
	}

	/**
	 * Returns the Mermaid representation of the TdSchema indicated in the instantiation.
	 * This string can be pasted in the mermaid live editor https://mermaid.live/
	 * or inserted in a Markdown documentation file
	 */
	public String getMermaid() {
		StringBuilder sb = new StringBuilder(); // Accumulates the mermaid written text
		sb.append("---"
				+ "\n  config:"
				+ "\n    class:"
				+ "\n      hideEmptyMembersBox: true"
				+ "\n---"); // to a more compact diagram
		sb.append("\nclassDiagram");
		if (direction == Direction.LEFT_RIGHT)
			sb.append("\n  direction LR");
		
		MermaidEntityWriter entityWriter = new MermaidEntityWriter(schema, sb);
		MermaidPathWriter pathWriter = new MermaidPathWriter(schema, sb);
		
		// Group entities in same operation+path to better layout (if configured)
		if (this.linkEntitiesInPath || this.boxEntitiesInPath)
			pathWriter.drawPathEntityLinks(this.linkEntitiesInPath, this.boxEntitiesInPath);
		
		// main, object, array and type entities with relations
		entityWriter.drawEntitiesAndRelations();
		
		// Add operations and paths (as methods)
		pathWriter.drawPathOperations();
		
		return sb.toString();
	}

}
