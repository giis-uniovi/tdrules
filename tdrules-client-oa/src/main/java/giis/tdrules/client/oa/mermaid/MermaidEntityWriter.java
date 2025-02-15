package giis.tdrules.client.oa.mermaid;

import java.util.HashSet;
import java.util.Set;

import giis.tdrules.client.oa.transform.UpstreamAttribute;
import giis.tdrules.model.shared.OaExtensions;
import giis.tdrules.openapi.model.TdAttribute;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;

/**
 * Produces a Mermaid string with a graphical representation of a TdSchema
 */
public class MermaidEntityWriter {

	private TdSchema schema;
	private StringBuilder sb; // Accumulates the mermaid written text
	private Set<String> drawn; // Already drawn entities

	public MermaidEntityWriter(TdSchema schema, StringBuilder sb) {
		this.schema = schema;
		this.sb = sb;
	}

	/**
	 * Handles the the drawing of entities, objects, arrays and types with their relations
	 */
	public void drawEntitiesAndRelations() {
		drawn = new HashSet<>();
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
