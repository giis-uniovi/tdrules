package giis.tdrules.client.oa.transform;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks the calls to extraction of nested objects (type and array) that are defined by a ref.
 * This allows to detect cycles among the refs and stop unnesting to prevent endless loops.
 */
public class RefStack {
	protected static final Logger log = LoggerFactory.getLogger(RefStack.class);

	private List<String> stack = new ArrayList<>();

	public void push(String ref) {
		if (ref != null)
			stack.add(ref);
	}

	public void pop(String ref) {
		if (ref != null)
			stack.remove(stack.size() - 1);
	}

	public boolean hasCycle(String ref) {
		if (ref != null && stack.contains(ref)) {
			log.warn("Detected a cyclic reference at {}, current stack: {}", ref, stack.toString());
			return true;
		}
		return false;
	}
}
