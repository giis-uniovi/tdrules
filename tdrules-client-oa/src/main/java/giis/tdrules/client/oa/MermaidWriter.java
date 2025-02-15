package giis.tdrules.client.oa;

import java.util.HashSet;
import java.util.Set;

import giis.tdrules.client.oa.mermaid.MermaidPathWriter;
import giis.tdrules.client.oa.transform.UpstreamAttribute;
import giis.tdrules.model.shared.OaExtensions;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;

/**
 * Produces a Mermaid string with a graphical representation of a TdSchema
 */
public class MermaidWriter {

	private TdSchema schema;
	private StringBuilder sb; // Accumulates the mermaid written text
	private Set<String> drawn; // Already drawn entities

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
	public MermaidWriter setLinkEntitiesInPath(boolean value) {
		this.linkEntitiesInPath = value;
		return this;
	}

	/**
	 * Groups in a box the entities that are in the same path and operation
	 */
	public MermaidWriter setGroupEntitiesInPath(boolean value) {
		this.boxEntitiesInPath = value;
		return this;
	}

	/**
	 * Returns the Mermaid representation of the TdSchema indicated in the instantiation.
	 * This string can be pasted in the mermaid live editor https://mermaid.live/
	 * or inserted in a Markdown documentation file
	 */
	public String getMermaid() {
		sb = new StringBuilder();
		MermaidPathWriter pathWriter = new MermaidPathWriter(schema, sb);
		drawn = new HashSet<>();
		sb.append("classDiagram");
		if (direction == Direction.LEFT_RIGHT)
			sb.append("\n  direction LR");
		
		// Group entities in same operation+path to better layout (if configured)
		if (this.linkEntitiesInPath || this.boxEntitiesInPath)
			pathWriter.drawPathEntityLinks(this.linkEntitiesInPath, this.boxEntitiesInPath);
		
		// First all entity relations and later the type definitions
		// to allow better separation in the mermaid display
		for (TdEntity entity : schema.getEntities())
			drawEntityRelations(entity);
		for (TdEntity entity : schema.getEntities())
			drawTypeDefinitions(entity);
		// If there is any entity not connected to any other, it is drawn at the end
		drawUnreferenced(schema);
		// Add notes for entities that have an extended attribute with the names of undefined referenced entities
		drawUndefinedRefs(schema);
		
		// Add operations and paths (as methods)
		pathWriter.drawPathOperations();
		
		return sb.toString();
	}

	private void drawEntityRelations(TdEntity entity) {
		for (TdAttribute attribute : giis.tdrules.model.shared.ModelUtil.safe(entity.getAttributes())) {
			if (attribute.isRid()) {
				// relation to another entity is drawn differently if it is from an array
				if (entity.isArray()) {
					// When the rid entity is not the immediate adjacent upstream, arrays have an extended attribute
					// to force drawing the visually correct relation that overrides the rid attribute
					String rid = attribute.getRidEntity();
					String drawRid = new UpstreamAttribute(schema).getMermaidRidEntity(entity);
					if (drawRid != null && !"".equals(drawRid))
						rid = drawRid;
					drawReferenceFromArray(entity.getName(), rid);
				} else {
					drawReferenceToEntity(entity.getName(), attribute.getRidEntity());
				}
			} else if (attribute.isType())
				drawCompositeType(attribute.getDatatype(), entity.getName());
		}
	}

	private void drawTypeDefinitions(TdEntity entity) {
		if ((entity.isArray() || entity.isType())
				&& (!"".equals(entity.getSubtype())))
			drawCompositeDefinition(entity.getName(), entity.getSubtype());
	}

	private void drawCompositeType(String contained, String container) {
		sb.append("\n  ").append(container).append(" *--\"1\" ").append(contained);
		drawnAdd(container, contained);
	}

	private void drawReferenceToEntity(String referencing, String referenced) {
		sb.append("\n  ").append(referenced).append(" <--\"*\" ").append(referencing);
		drawnAdd(referencing, referenced);
	}

	private void drawReferenceFromArray(String referencing, String referenced) {
		sb.append("\n  ").append(referenced).append(" *--\"*\" ").append(referencing);
		drawnAdd(referencing, referenced);
	}

	private void drawCompositeDefinition(String contained, String container) {
		sb.append("\n  ").append(contained).append(" ..|> ").append(container);
		drawnAdd(container, contained);
	}

	private void drawUnreferenced(TdSchema schema) {
		for (TdEntity entity : schema.getEntities())
			if (!drawn.contains(entity.getName()))
				sb.append("\n  class ").append(entity.getName());
	}
	
	private void drawUndefinedRefs(TdSchema schema) {
		for (TdEntity entity : schema.getEntities()) {
			String refs = entity.getExtendedItem(OaExtensions.UNDEFINED_REFS);
			if (refs != null)
				sb.append("\n  note for ").append(entity.getName())
					.append(" \"").append("Undefined $ref:<br/>")
					.append(refs.replace(",", "<br/>")).append("\"");
		}
	}

	private void drawnAdd(String ref1, String ref2) {
		drawn.add(ref1);
		drawn.add(ref2);
	}

}
