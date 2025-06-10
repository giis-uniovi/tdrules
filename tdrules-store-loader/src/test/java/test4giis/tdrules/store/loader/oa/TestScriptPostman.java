package test4giis.tdrules.store.loader.oa;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import giis.tdrules.openapi.model.TdSchema;
import giis.tdrules.store.loader.DataLoader;
import giis.tdrules.store.loader.gen.IDataAdapter;
import giis.tdrules.store.loader.oa.OaLiveUidGen;
import giis.tdrules.store.loader.oa.script.PostmanAdapter;

/**
 * Main tests for zerocode scripts are based on the tests for live backend uid generation
 * by inheriting its behaviour. Assertions are made on the scenario file contents.
 */
public class TestScriptPostman extends TestOaLiveBackendUid {
	
	// Configuration is with the Zerocode adapter and a fixed url, no api mocking
	
	@BeforeClass
	public static void setUpClass() {
		// not using mocks
	}
	@AfterClass
	public static void tearDownClass() throws SQLException {
		// not using mocks
	}

	@Override
	protected DataLoader getLiveGenerator(TdSchema schema) {
		IDataAdapter dataAdapter=new PostmanAdapter(getServerUrl());
		return new DataLoader(schema, dataAdapter).setUidGen(new OaLiveUidGen());
	}
	@Override
	protected String getServerUrl() {
		return "http://127.0.0.1";
	}
	@Override
	protected void mockPost(String endpointPath, String requestBody, String responseBody) {
		// not using mocks
	}
	
	// Reused methods are like in base class, but values of uids are tokens to reference responses in previous steps
	
	@Override
	protected void assertSymbols(DataLoader dtg, int symbolCount, String detailName) {
		assertEquals(symbolCount, dtg.getSymbolicKeyValues().size());
		assertJson("{{step0_master1}}", dtg.getSymbolicKeyValues().get("master1.pk1.@km11"));
		assertJson("{{step1_master1}}", dtg.getSymbolicKeyValues().get("master1.pk1.@km12"));
		assertJson("{{step2_master2}}", dtg.getSymbolicKeyValues().get("master2.pk2.@km21"));
		assertJson("{{step3_master2}}", dtg.getSymbolicKeyValues().get("master2.pk2.@km22"));
		if (symbolCount == 4)
			return;
		assertJson("{{step4_" + detailName + "}}", dtg.getSymbolicKeyValues().get(detailName +".dk.@dk1"));
		assertJson("{{step5_" + detailName + "}}", dtg.getSymbolicKeyValues().get(detailName +".dk.@dk2"));
	}
	@Override
	protected void assertAll(DataLoader dtg, String detailName) throws IOException {
		assertAllFiles(dtg, "script-postman-" + detailName + ".json");
	}
	
	@Test
	@Override
	public void testLiveBackUidBase() throws IOException {
		super.testLiveBackUidBase();
	}

	@Test
	@Override
	public void testLiveBackUidRidsAreUids() throws IOException {
		super.testLiveBackUidRidsAreUids();
	}
	
	@Test
	@Override
	public void testLiveBackUidPathParams() throws IOException {
		super.testLiveBackUidPathParams();
	}


	@Test
	@Override
	public void testLiveNestedObjectBackUid() throws IOException {
		super.testLiveNestedObjectBackUid();
	}
	@Override
	protected void assertNestedObject(DataLoader dtg) throws IOException {
		assertAllFiles(dtg, "script-postman-nestedobj.json");
	}

}
