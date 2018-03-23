package bgu.spl.a2;

import java.util.concurrent.*;
import java.util.Vector;
import java.util.NoSuchElementException;
import java.util.Random;


/**
 * represents a work stealing thread pool - to understand what this class does
 * please refer to your assignment.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class WorkStealingThreadPool {

	/**
	 * @param threads - all the threads runniung the processors in the pool
	 */
	private Vector<Thread> threads = new Vector<Thread>();
	
	/**
	 * @param queues - all the queues of tasks of the processors in the pool
	 */
	private Vector<ConcurrentLinkedDeque<Task<?>>> queues = new Vector<ConcurrentLinkedDeque<Task<?>>>();
	
	/**
	 * @param processors - all the processors in the pool
	 */
	private Processor [] processors;
	
	/**
	 * @param vm - the monitor (version) of the pool
	 */
	private VersionMonitor vm = new VersionMonitor();
	
	
    /**
     * creates a {@link WorkStealingThreadPool} which has n threads
     * {@link Processor}s. Note, threads should not get started until calling to
     * the {@link #start()} method.
     *
     * Implementors note: you may not add other constructors to this class nor
     * you allowed to add any other parameter to this constructor - changing
     * this may cause automatic tests to fail..
     *
     * @param nthreads the number of threads that should be started by this
     * thread pool
     */
	public WorkStealingThreadPool(int nthreads) {
		processors = new Processor[nthreads];

		for (int i = 0; i < nthreads; i++) {
			processors[i] = new Processor(i, this);
			queues.add(new ConcurrentLinkedDeque<Task<?>>());
			threads.add(new Thread(processors[i]));
		}
	}

    /**
     * submits a task to be executed by a processor belongs to this thread pool
     *
     * @param task the task to execute
     */
    public void submit(Task<?> task) {
    	Random rand = new Random();
    	int n = processors.length - 1;
    	int r=0;
    	if(n>0){ r = rand.nextInt(n);
        queues.elementAt(r).addFirst(task);
    	}
    	else {queues.elementAt(n).addFirst(task);}
        vm.inc();
    }

    /**
     * closes the thread pool - this method interrupts all the threads and wait
     * for them to stop - it is returns *only* when there are no live threads in
     * the queue.
     *
     * after calling this method - one should not use the queue anymore.
     *
     * @throws InterruptedException if the thread that shut down the threads is
     * interrupted
     * @throws UnsupportedOperationException if the thread that attempts to
     * shutdown the queue is itself a processor of this queue
     */
    public void shutdown() throws InterruptedException {
    	try{
    		for(Thread t : threads){
    			if(Thread.currentThread() == t) throw new UnsupportedOperationException("Processor can NOT shutdown himself");
    			t.interrupt();
    		}
        
    		for(Thread tt : threads){
    			tt.join();
    		}
    	} catch (InterruptedException e){}
    }

    /**
     * start the threads belongs to this thread pool
     */
    public void start() {
        for( Thread t : threads){
        	t.start();
        }
    }
    
    /** 
     * @param id - id of the processor
     * @return number of current task in id's queue
     */
    /*package*/ int HowMuchTasksInQueue(int id){
    	return (queues.elementAt(id)).size();
    }
    
    /**
     * steal method
     * @param id - the id of the processor that commit the stealing
     * @return one task in hand (that was stolen) ready to be run by the thief
     */
    private Task<?> steal(int id) throws InterruptedException {
    	int ind = (id + 1) % queues.size();
    	int howMuchTasksToSteal = 0;
    	Vector<Task<?>> vec = new Vector<>();
    	
    	Task<?> ans = null;
    	while(ans==null && !Thread.currentThread().isInterrupted()){
    		ConcurrentLinkedDeque<Task<?>> q = queues.elementAt(ind);
    		
    				if(ind == id){
						try {
							vm.await(vm.getVersion()); // wait for a task to be added to the pool
						} catch (InterruptedException e) { throw e;}
						
						try{
							if(queues.elementAt(id).size()>0) ans = queues.elementAt(id).pop();
							
						} catch(Exception e){ ans = null;}
    				}
    				
    				if(ans!=null) break;
    				
    				else{
    					if(ind!=id){
    						howMuchTasksToSteal = (int)(q.size()/2)-1;
    						if(ans==null) ans = q.pollLast();
    						if(ans!=null){
    							for(int i = 0; i<howMuchTasksToSteal; i++){
    								Task<?> nt = q.pollLast();
    								if(nt != null) vec.addElement(nt);
    								else break;
    							}
    					
    							for( Task<?> t : vec){
    								submit(t, id);
    							}
    						}
    					}
    				}

    		ind = (ind + 1) % queues.size();
    	}
    	return ans;
    }
    
    
    /**
     * returning a task from id's queue
     * returns null if there aren't tasks in the queue
     */
    /*package*/ Task<?> giveTask(int id) throws InterruptedException {
    	
    	Task<?> ans = null;
    	Throwable i = null;
    	
		try{
			ans = (queues.elementAt(id)).pop();
		} catch (Exception e) { i=e; }
		if(i instanceof NoSuchElementException){
			try{
			ans = steal(id);
			} catch (InterruptedException e) { throw e; }
		}
		
		if(i instanceof InterruptedException) throw new InterruptedException();
		
    	return ans;
    }

    
    /**
     * this function submits (push) a task to id's queue
     * @param t - the task to be pushed
     * @param id - the id of the processor to submit the task to
     */
    /*package*/ void submit(Task<?> t, int id){
    	(queues.elementAt(id)).addFirst(t);
    	vm.inc();
    }

}
