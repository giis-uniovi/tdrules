package giis.tdrules.client.oa.transform;

import giis.tdrules.model.shared.OaExtensions;
import giis.tdrules.openapi.model.TdEntity;
import giis.tdrules.openapi.model.TdSchema;

/**
 * During transformations, a common operation is to move up to the entities
 * hierarchy. The upstream attribute allows this. However, not always the
 * upstream is the immediate parent (e.g. an array inside an object must
 * reference the parent of the containing object, not the object).
 * 
 * As the upstream is not an attribute of the TDM, this class provides utility
 * methods to manage the upstream attribute, that is stored in extended
 * properties.
 */
public class UpstreamAttribute {
	private static final String UPSTREAM = OaExtensions.UPSTREAM;
	private TdSchema dbSchema;

	public UpstreamAttribute(TdSchema dbSchema) {
		this.dbSchema = dbSchema;
	}

	/**
	 * Returns the upstream entity of a given entity, null if there are no upstream
	 */
	public TdEntity getUpstream(TdEntity thisEntity) {
		String upstream = thisEntity.getExtendedItem(UPSTREAM);
		return dbSchema.getEntityOrNull(upstream);
	}

	/**
	 * Sets the name of the upstream entity
	 */
	public void setUpstream(TdEntity thisEntity, String upsteramEntity) {
		thisEntity.putExtendedItem(UPSTREAM, upsteramEntity);
	}

	/**
	 * Remove the upstream attribute
	 */
	public void removeUpstream(TdEntity thisEntity) {
		thisEntity.getExtended().remove(UPSTREAM);
	}

	/**
	 * Navigates up to the hierarchy to get the first upstream that has unique
	 * identifiers
	 */
	public TdEntity findUpstreamWithUid(TdEntity entity) {
		TdEntity upstream = getUpstream(entity);
		if (upstream == null)
			return null;
		return upstream.getUid() != null ? upstream : findUpstreamWithUid(upstream);
	}
	
}
