package giis.m1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Class1 {
	private static final Logger log=LoggerFactory.getLogger(Class1.class);
	
	public String function11() {
		log.info("Run function11()");
		return "11";
	}
	public String function12() {
		log.info("Run function12()");
		return "12";
	}

}
