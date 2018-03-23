package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VersionMonitorTest {
	VersionMonitor vM;
	@Before
	public void setUp() throws Exception {
		VersionMonitor vM=new VersionMonitor();
	}

	@After
	public void tearDown() throws Exception {
		vM=null;
	}

	/**
	 * Tests if the version is >= 0
	 */
	@Test
	public void testGetVersion() {
		assertTrue(vM.getVersion() >=0);
	}
	

	/**
	 * Tests if inc() does make the version number greater by 1
	 */
	@Test
	public void testInc() {
		int curr = vM.getVersion();
		vM.inc();
		assertTrue(curr+1 == vM.getVersion());
	}

	/**
	 * Tests if await() does force a thread to wait till a specific version achieved
	 */
	@Test
	public void testAwait() {
		int myVersion = vM.getVersion();
		Thread t = new Thread(() -> {
			try {
				//wait until this version number is changed
				vM.await(myVersion);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		t.start();
		vM.inc();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertTrue (myVersion != vM.getVersion());
	}
	
}
