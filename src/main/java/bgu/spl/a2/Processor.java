package bgu.spl.a2;

/**
 * this class represents a single work stealing processor, it is
 * {@link Runnable} so it is suitable to be executed by threads.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 *
 */
public class Processor implements Runnable {

    private final WorkStealingThreadPool pool;
    private final int id;
    private VersionMonitor howMuchStealers = new VersionMonitor();
    

    /**
     * constructor for this class
     *
     * IMPORTANT:
     * 1) this method is package protected, i.e., only classes inside
     * the same package can access it - you should *not* change it to
     * public/private/protected
     *
     * 2) you may not add other constructors to this class
     * nor you allowed to add any other parameter to this constructor - changing
     * this may cause automatic tests to fail..
     *
     * @param id - the processor id (every processor need to have its own unique
     * id inside its thread pool)
     * @param pool - the thread pool which owns this processor
     */
    /*package*/ Processor(int id, WorkStealingThreadPool pool) {
        this.id = id;
        this.pool = pool;
    }

	@Override
    public void run() {
    	while(!Thread.currentThread().isInterrupted()){
    		Task<?> t = null;
    		try{
    		t = pool.giveTask(id);
    		} catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    		if(t != null){
    			t.handle(this);
    		}
    	}
    }
    
    /* package */ WorkStealingThreadPool getPool(){
    	return pool;
    }
    
    /* package */ int getId(){
    	return id;
    }
    
    /**
     * ask the pool for adding a new task to this processor
     * @param t - the task to be added
     */
    /* package */ void submit(Task<?> t){
    	pool.submit(t, id); // adds the task t to the queue of this processor
    }
}
