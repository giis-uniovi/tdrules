package giis.tdrules.client.oa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import giis.tdrules.client.oa.transform.UpstreamAttribute;
import giis.tdrules.model.shared.OaExtensions;
import giis.tdrules.openapi.model.Ddl;
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
	
	public MermaidWriter setLeftToRight() {
		this.direction = Direction.LEFT_RIGHT;
		return this;
	}
	
	public MermaidWriter setGroupEntitiesInPath(boolean link, boolean box) {
		this.linkEntitiesInPath = link;
		this.boxEntitiesInPath = box;
		return this;
	}

	/**
	 * Returns the Mermaid representation of the TdSchema indicated in the instantiation.
	 * This string can be pasted in the mermaid live editor https://mermaid.live/
	 * or inserted in a Markdown documentation file
	 */
	public String getMermaid() {
		sb = new StringBuilder();
		drawn = new HashSet<>();
		sb.append("classDiagram");
		if (direction == Direction.LEFT_RIGHT)
			sb.append("\n  direction LR");
		
		// Group entities in same operation+path to better layout (if configured)
		if (this.linkEntitiesInPath || this.boxEntitiesInPath)
			drawPathEntityLinks();
		
		// First all entity relations and later the type definitios
		// to allow better separation in the mermaid display
		for (TdEntity entity : schema.getEntities())
			drawEntityRelations(entity);
		for (TdEntity entity : schema.getEntities())
			drawTypeDefinitions(entity);
		// If there is any entity not connected to any other, it is drawn at the end
		drawUnreferenced(schema);
		// Add notes for entities that have an extended attribute with the names of undefined referenced entities
		drawUndefinedRefs(schema);
		
		// Schema may contain information about the paths for POST operations,
		// add them to the mermaid diagram as methods
		for (TdEntity entity : schema.getEntities())
			drawPostOperations(entity);
		
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

	private void drawPostOperations(TdEntity entity) {
		for (Ddl operation : giis.tdrules.model.shared.ModelUtil.safe(entity.getDdls()))
			drawMethod(entity.getName(), operation.getCommand(), operation.getQuery());
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

	private void drawMethod(String entity, String operation, String arguments) {
		sb.append("\n  ").append(entity).append(": +")
		.append(operation).append("(").append(arguments).append(")");
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
	
	// Add links or boxes pairing entities that are in the same path+operation
	private void drawPathEntityLinks() {
		// group by path before drawing, each pair of entities in a path is candidate to be linked or boxed
		Map<String, String[]> paths = groupByPaths();
		drawPathEntityLinks(paths, "post"); // prioritizes post over put
		drawPathEntityLinks(paths, "put");
	}
	private Map<String, String[]> groupByPaths() {
		Map<String, String[]> paths = new LinkedHashMap<>();
		for (TdEntity entity : schema.getEntities()) {
			for (Ddl ddl : entity.getDdls()) {
				String key = ddl.getCommand().toLowerCase() + " " + ddl.getQuery();
				String[] current = paths.get(key);
				if (current == null) {
					// first entity in path (either request or response), create path and add
					paths.put(key, new String[] { entity.getName(), null });
				} else if (!entity.getName().equals(current[0]))
					// second entity, add only if different from the first one (request!=response)
					current[1] = entity.getName();
			}
		}
		return paths;
	}

	private void drawPathEntityLinks(Map<String, String[]> paths, String operation) {
		Map<String, String> boxedEntities = new HashMap<>();
		int namespaceCount = 0;
		for (Map.Entry<String, String[]> entry : paths.entrySet()) {
			// to allow prioritize get operations, first this is called with post, then with put
			if (!entry.getKey().startsWith(operation.toLowerCase()))
				break;

			// Draw only if there are two entities, i.e. request!=response
			String[] entities = entry.getValue();
			if (entities[0] != null && entities[1] != null) {
				drawPathEntitiesLink(entities[0], entities[1], operation, boxedEntities, namespaceCount);
				namespaceCount++; // required to do not duplicate names in boxes
			}
		}
	}

	private void drawPathEntitiesLink(String entity1, String entity2, String operation, Map<String, String> boxedEntities, int namespaceCount) {
		if (this.linkEntitiesInPath) {
			sb.append("\n  ").append(entity1).append(" .. ")
				.append(entity2).append(" : ").append(operation);
		}
		// Boxed entities require that both entities have not added previously to any box
		if (this.boxEntitiesInPath && !boxedEntities.containsKey(entity1) && !boxedEntities.containsKey(entity2)) { // NOSONAR
			sb.append("\n  namespace ").append(operation).append(namespaceCount).append(" {")
				.append("\n    class ").append(entity1)
				.append("\n    class ").append(entity2)
				.append("\n  }");
			boxedEntities.put(entity1, operation);
			boxedEntities.put(entity2, operation);
		}
	}
}
