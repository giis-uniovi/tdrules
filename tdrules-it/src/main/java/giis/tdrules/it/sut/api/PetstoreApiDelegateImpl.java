package giis.tdrules.it.sut.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import giis.tdrules.it.sut.model.Category;
import giis.tdrules.it.sut.model.Pet;
import giis.tdrules.it.sut.model.PetstoreAll;
import giis.tdrules.it.sut.model.Tagx;

/**
 * The implementation of the business logic and storage for the peststore IT SUT.
 * Stores the data and the last generated uids in memory.
 * The actual data retrieved by getPetstoreAll and reset by deletePetstoreAll
 */
@Service
public class PetstoreApiDelegateImpl implements PetstoreApiDelegate {
	private final static Logger log=LoggerFactory.getLogger(PetstoreApiDelegateImpl.class);
	
	private static int petId;
	private static int categoryId;
	private static int tagxId;
	private static PetstoreAll db;
	
	static {
		initialize();
	}
	
	private static void initialize() {
		log.info("Initialize PetstoreApiDelegate");
		petId = 0;
		categoryId = 0;
		tagxId = 0;
		db = new PetstoreAll();
	}
	
	@Override
	public ResponseEntity<PetstoreAll> getPetstoreAll() {
		log.debug("Call api delegate: getAll");
		return ResponseEntity.ok(db);
	}
	
	@Override
	public ResponseEntity<Void> deletePetstoreAll() {
		log.debug("Call api delegate: deleteAll");
		initialize();
        return new ResponseEntity<Void>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Category> createCategory(Category category) {
		log.debug("Call api delegate: createCategory: {}", category.getName());
		categoryId++;
		Category newCategory = new Category().id(categoryId*100 + categoryId).name(category.getName());
		db.addCategoryItem(newCategory);
		return ResponseEntity.ok(newCategory);
	}

	@Override
	public ResponseEntity<Tagx> createTagx(Tagx tagx) {
		log.debug("Call api delegate: createTagx: {}", tagx.getName());
		tagxId++;
		Tagx newTagx = new Tagx().id(tagxId*1000 + tagxId).name(tagx.getName());
		db.addTagxItem(newTagx);
		return ResponseEntity.ok(newTagx);
	}

	@Override
	public ResponseEntity<Pet> createPet(Pet pet) {
		log.debug("Call api delegate: createPet: {}", pet.getName());
		petId++;
		pet.id(petId);
		db.addPetItem(pet);
		return ResponseEntity.ok(pet);
	}

}
