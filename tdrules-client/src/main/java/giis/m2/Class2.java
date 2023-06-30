package giis.m2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.m1.Class1;

public class Class2 {
	private static final Logger log=LoggerFactory.getLogger(Class1.class);
	public String function21() {
		log.info("Run function21()");
		Class1 class1 = new Class1();
		return class1.function11() + "-" + class1.function12();
	}
}
