package giis.tdrules.it.sut.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import giis.tdrules.it.sut.model.CreateProjRequest;
import giis.tdrules.it.sut.model.CreateWorkRequest;
import giis.tdrules.it.sut.model.Proj;
import giis.tdrules.it.sut.model.Staff;
import giis.tdrules.it.sut.model.Work;
import giis.tdrules.it.sut.model.WorkplaceAll;

/**
 * The implementation of the business logic and storage for the workspace IT SUT.
 * Stores the data and the last generated uids in memory.
 * The actual data retrieved by getWorkspaceAll and reset by deleteWorkspaceAll
 */
@Service
public class WorkplaceApiDelegateImpl implements WorkplaceApiDelegate {
	private final static Logger log=LoggerFactory.getLogger(WorkplaceApiDelegateImpl.class);
	
	private static long staffId;
	private static long projId;
	private static WorkplaceAll db;
	
	static {
		initialize();
	}
	
	private static void initialize() {
		log.info("Initialize WorkplaceApiDelegate");
		staffId = 0;
		projId = 0;
		db = new WorkplaceAll();
	}
	
	@Override
	public ResponseEntity<WorkplaceAll> getAll() {
		log.debug("Call api delegate: getAll");
		return ResponseEntity.ok(db);
	}
	
	@Override
	public ResponseEntity<Void> deleteAll() {
		log.debug("Call api delegate: deleteAll");
		initialize();
        return new ResponseEntity<Void>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Staff> createStaff(Staff staff) {
		log.debug("Call api delegate: createStaff: {}", staff.getName());
		staffId++;
		Staff newStaff = new Staff().id(staffId*1000+staffId).name(staff.getName());
		db.addStaffItem(newStaff);
		return ResponseEntity.ok(newStaff);
	}

	@Override
	public ResponseEntity<Proj> createProj(CreateProjRequest createProjRequest) {
		log.debug("Call api delegate: createProj: {}", createProjRequest.getName());
		projId++;
		Proj newProj = new Proj().id("PR0" + projId).name(createProjRequest.getName());
		db.addProjItem(newProj);
		return ResponseEntity.ok(newProj);
	}

	@Override
    public ResponseEntity<Work> createWork(String projId, CreateWorkRequest createWorkRequest) {
		log.debug("Call api delegate: createWork: {}", createWorkRequest.getDays());
		Work newWork = new Work().projId(projId)
				.staffId(createWorkRequest.getStaffId())
				.days(createWorkRequest.getDays());
		db.addWorkItem(newWork);
		return null;
	}
}
