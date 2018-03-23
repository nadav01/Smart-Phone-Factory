package bgu.spl.a2;

import java.util.concurrent.atomic.AtomicInteger; 

/**
 * Describes a monitor that supports the concept of versioning - its idea is
 * simple, the monitor has a version number which you can receive via the method
 * {@link #getVersion()} once you have a version number, you can call
 * {@link #await(int)} with this version number in order to wait until this
 * version number changes.
 *
 * you can also increment the version number by one using the {@link #inc()}
 * method.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class VersionMonitor {
	
	 AtomicInteger v =new AtomicInteger(0);

	/**
	 *  @return the current version
	 * @pre: getVersion() >=0
	 * @post: none
	 */
	public int getVersion() {
		return v.get();
	}

	/**
	 *  Increments the version
	 * @pre: none
	 * @post: get() == @pre(get())+1
	 */
	public synchronized void inc() {
		v.incrementAndGet();
		this.notifyAll();
	}

	/**
	 * Causing the call to wait until the version is no longer equals to @version
	 * This function is synchronized in order to be able to wait.
	 * @pre: getVersion() <= @param version & @param version>=0
	 * @post: none
	 */
	public void await(int version) throws InterruptedException {
		while(version == v.get() && Thread.currentThread().isInterrupted() == false){
			synchronized(this){
			try{
			this.wait(); // causing the running thread \ processor to wait
			} catch (InterruptedException e) { throw e;}
			}
		}
	}
}
