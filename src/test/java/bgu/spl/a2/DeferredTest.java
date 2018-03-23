package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeferredTest {

	Deferred<Integer> d;
	
	@Before
	public void setUp() throws Exception {
		Deferred<Integer> d = new Deferred<>();
	}

	@After
	public void tearDown() throws Exception {
		d = null;
	}

	
	// ##### get() Testing Area #####
	
	/**
	 * tests if the get() throws an error when called before resolve()
	 */
	@Test
	public void testGet() {
		Throwable e = null;
		try{
			d.get();
		} catch (Throwable i){
			e = i;
		}
		
		assertTrue("The get() should trow a IllegalStateException", e instanceof IllegalStateException);
	}
	
	
	/**
	 * tests if the get() returns the right result set by resolve()
	 */
	@Test
	public void testGet2() {
		d.resolve(new Integer(5));
		assertEquals("The get() should return 5", new Integer(5), d.get());
	}

	

	
	// ##### isResolved() Testing Area #####
	
	/**
	 * tests if the isResolved() returns false if resolved() hasn't called yet
	 */
	@Test
	public void testIsResolved() {
		assertFalse("isResolved() should be false", d.isResolved());
	}
	
	/**
	 * tests if the isResolved() returns true after resolved() has called
	 */
	@Test
	public void testIsResolved2() {
		d.resolve(new Integer(1));
		assertTrue("isResolved() should be true", d.isResolved());
	}

	

	
	// ##### resolve() Testing Area #####
	
	/**
	 * tests if resolve() throws an error when null sent
	 */
	@Test
	public void testResolve() {
		Integer k = null;
		Throwable e = null;
		try{ d.resolve(k);
		} catch (Throwable i){
			e = i;
		}
		assertTrue("resolve() should throw a NullPointerException", e instanceof NullPointerException);
	}
	
	/**
	 * tests if resolve() changes the result of Deferred
	 */
	@Test
	public void testResolve2() {
		d.resolve(new Integer (23));
		assertEquals("resolve() should make the get() return 23", new Integer(23), d.get());
	}
	
	/**
	 * tests if resolve() notifies a waiting thread
	 */
	@Test
	public void testResolve3() {
		d.whenResolved(() -> this.notify() ); // when the deferred resolve it notifies a waiting thread
		Thread t = new Thread(() -> {
				synchronized(d){
					while(!d.isResolved()){
						try{
							d.wait();
						} catch (InterruptedException e) {}
					}
				}
		});
		t.start(); // at this point t is waiting
		d.resolve(new Integer(1)); // t is getting awake
		try{
		t.join(); // waits for t to die
		} catch (InterruptedException e) {e.printStackTrace();}
		assertFalse("The thread suppose to be dead", t.isAlive());
	}


	

	
	// ##### whenResolved() Testing Area #####
	
	/**
	 * tests if a callback sent to whenResolved() being executed when calling resolve()
	 */
	@Test
	public void testWhenResolved() {
		Runnable r = () -> this.notify();
		d.whenResolved(r);
		
		Runnable st = () -> {
			synchronized(d){
				while(!d.isResolved()){
					try{
						d.wait();
					} catch (InterruptedException e) {}
				}
			}
		};
	
	Thread t1 = new Thread(st);
	t1.start();
	
	d.resolve(new Integer(1)); // should notify t1
	try {
		t1.join(); // waits for t1 to die
	} catch (InterruptedException e) {
				e.printStackTrace();
	}
		
		assertFalse("t1 should be dead", t1.isAlive());
	}
	
	/**
	 * tests if whenResolved() throws an error when receives null
	 */
	@Test
	public void testWhenResolved2() {
		Runnable r = null;
		Exception i = null;
		try{ d.whenResolved(r);
		} catch (Exception e){
			i = e;
		}
		assertTrue("whenResolve() should throw a NullPointerException", i instanceof NullPointerException);
	}
}
