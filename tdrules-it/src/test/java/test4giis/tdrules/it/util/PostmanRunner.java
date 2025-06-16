package test4giis.tdrules.it.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.tdrules.store.loader.shared.LoaderException;

/**
 * Runs a Postman collection from the cli using newman (mvn install -g newman).
 * It does not guarantees test failure if something fails, requires further verification of generated test data.
 */
public class PostmanRunner {
	Logger log=LoggerFactory.getLogger(this.getClass());

	public void run(String collection) throws IOException {
		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		String[] command = isWindows
				? new String[] {"cmd", "/c", "newman run " + collection.replace("/", "\\\\")}
				: new String[] {"/bin/bash", "-c", "newman run " + collection};
		log.info("Running collection, command: {}", Arrays.toString(command));
		Process proc = Runtime.getRuntime().exec(command);
		
		try {
			proc.waitFor();
		} catch (InterruptedException e) {
			throw new LoaderException(e);
		}

		System.out.println("Error output from command:");
		printExecOutput(proc.getErrorStream());
		System.out.println("Standard output from command:");
		printExecOutput(proc.getInputStream());
	}
	
	private void printExecOutput(InputStream stream) throws IOException {
		BufferedReader stdInput = new BufferedReader(
				new InputStreamReader(stream));
		String s;
		while ((s = stdInput.readLine()) != null) {
			System.out.println(s);
		}
	}
	
}
