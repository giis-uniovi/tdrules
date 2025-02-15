package giis.tdrules.client.oa.mermaid;

import java.util.LinkedHashMap;
import java.util.Map;

import giis.tdrules.model.shared.ModelUtil;
import giis.tdrules.openapi.model.Ddl;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;

/**
 * Handles the the drawing of links and groups (boxes) that enclose 
 * entities that are in the same path (opt-in)
 */
public class MermaidPathWriter {

	private TdSchema schema;
	private StringBuilder sb;
	
	// Tracks how many times an entity has been processed.
	// When an entity is inside a box, the first time it is drawn normally,
	// next times a "cloned" entity (with dashed borders) must be displayed 
	// (if not, mermaid overrides the all previous displays)
	private class BoxedEntities {
		private Map<String, Integer> entities = new LinkedHashMap<>();
		private void add(String entity, String operation) {
			String key = entity + " " + operation;
			entities.put(key, get(entity, operation) == 0 ? 1 : get(entity, operation) + 1);
		}
		private int get(String entity, String operation) {
			String key = entity + " " + operation;
			return entities.get(key) == null ? 0 : entities.get(key);
		}
	}
	
	public MermaidPathWriter(TdSchema schema, StringBuilder sb) {
		this.schema = schema;
		this.sb = sb;
	}
	
	public void drawPathOperations() {
		for (TdEntity entity : schema.getEntities())
			for (Ddl operation : ModelUtil.safe(entity.getDdls()))
				drawMethod(entity.getName(), operation.getCommand(), operation.getQuery());
	}

	/**
	 * Add links or boxes pairing entities that are in the same path+operation
	 */
	public void drawPathEntityLinks(boolean link, boolean box) {
		// group by path before drawing, each pair of entities in a path is candidate to be linked or boxed
		Map<String, String[]> paths = groupByPaths();
		drawPathEntityLinks(paths, "post", link, box); // prioritizes post over put
		drawPathEntityLinks(paths, "put", link, box);
	}
	
	private Map<String, String[]> groupByPaths() {
		Map<String, String[]> paths = new LinkedHashMap<>();
		for (TdEntity entity : schema.getEntities()) {
			for (Ddl ddl : entity.getDdls()) {
				String key = ddl.getCommand().toLowerCase() + " " + ddl.getQuery();
				String[] current = paths.get(key);
				if (current == null)
					// first entity in path (either request or response), create path and add
					paths.put(key, new String[]{entity.getName(), null});
				else if (!entity.getName().equals(current[0]))
					// second entity, add only if different from the first one (request!=response)
					current[1] = entity.getName();
			}
		}
		return paths;
	}

	private void drawPathEntityLinks(Map<String, String[]> paths, String operation, boolean link, boolean box) {
		BoxedEntities boxedEntities = new BoxedEntities();
		for (Map.Entry<String, String[]> entry : paths.entrySet()) {
			// to allow prioritize get operations, first this is called with post, then with put
			if (!entry.getKey().startsWith(operation.toLowerCase()))
				continue;

			String[] entities = entry.getValue();
			// Entity1 is always present (if not there is no key and this method is not called, but entity2 can be null
			if (link)
				drawLink(entities[0], entities[1], operation);
			if (box)
				drawBox(entities[0], entities[1], operation,boxedEntities, entry.getKey());
		}
	}

	private void drawLink(String entity1, String entity2, String operation) {
		// Draw link only if there are two entities and they are different
		if (entity2 != null && !entity1.equals(entity2))
			drawEntitiesLink(entity1, entity2, operation);
	}

	private void drawBox(String entity1, String entity2, String operation, BoxedEntities boxed, String path) {
		// Boxed entities require that both entities have not added previously to any box
		String entity1name = boxed.get(entity1, operation) > 0
				? (entity1 + "_r" + boxed.get(entity1, operation))
				: entity1;
		String entity2name = null;
		if (entity2 != null)
			entity2name = boxed.get(entity2, operation) > 0
					? (entity2 + "_r" + boxed.get(entity2, operation))
					: entity2;

		drawEntitiesBox(entity1name, entity2name, path);

		if (boxed.get(entity1, operation) > 0)
			drawEntitiesRepeatedBox(entity1name);
		if (entity2name != null && boxed.get(entity2, operation) > 0)
			drawEntitiesRepeatedBox(entity2name);

		boxed.add(entity1, operation);
		if (entity2 != null)
			boxed.add(entity2, operation);
	}

	private void drawMethod(String entity, String operation, String arguments) {
		sb.append("\n  ").append(entity).append(": +")
		.append(operation).append("(").append(arguments).append(")");
	}
	
	private void drawEntitiesLink(String entity1, String entity2, String operation) {
		sb.append("\n  ").append(entity1).append(" .. ").append(entity2)
			.append(" : ").append(operation);
	}
	
	private void drawEntitiesBox(String entity1, String entity2, String path) {
		path = path.replaceAll("[^A-Za-z0-9\\-]", "_"); // mermaid requirement for names
		sb.append("\n  namespace ").append(path).append(" {");
		sb.append("\n    class ").append(entity1);
		if (entity2 != null)
			sb.append("\n    class ").append(entity2);
		sb.append("\n  }");
	}

	private void drawEntitiesRepeatedBox(String entity) {
		sb.append("\n  style ").append(entity)
			.append(" fill:#fff,stroke:#333,stroke-width:1px,stroke-dasharray: 5 5;");
	}

}
