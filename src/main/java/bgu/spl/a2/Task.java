package bgu.spl.a2;

import java.util.Collection;

/**
 * an abstract class that represents a task that may be executed using the
 * {@link WorkStealingThreadPool}
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add to this class can
 * only be private!!!
 *
 * @param <R> the task result type
 */
public abstract class Task<R> {
	
	/**
	 * @param p - the processor handling the task
	 */
	private Processor p = null;
	
	/**
	 * @param d - the deferred object of the task
	 */
	private Deferred<R> d = new Deferred<R>();
	
	/**
	 * @param done - the callback to run when all the subtasks resolved
	 */
	private Runnable done = null;
	
	/**
	 * @param check - a shared object that submitting this task
	 * back to the pool when all the subtasks resolved
	 */
	private Checker check;
	
	private boolean hasStarted = false;
	
    /**
     * start handling the task - note that this method is protected, a handler
     * cannot call it directly but instead must use the
     * {@link #handle(bgu.spl.a2.Processor)} method
     */
    protected abstract void start();

    /**
     *
     * start/continue handling the task
     *
     * this method should be called by a processor in order to start this task
     * or continue its execution in the case where it has been already started,
     * any sub-tasks / child-tasks of this task should be submitted to the queue
     * of the handler that handles it currently
     *
     * IMPORTANT: this method is package protected, i.e., only classes inside
     * the same package can access it - you should *not* change it to
     * public/private/protected
     *
     * @param handler the handler that wants to handle the task
     * 
     * This function is synchronized because we don't want two processors
     * trying to steal the task in the same time
     * 
     * This is the only function that can be called by two different threads (processors)
     */
    /*package*/ synchronized final void handle(Processor handler) {
    	p = handler;
    	if(hasStarted && !d.isResolved()) done.run(); // NOTE: if the task is being stolen, its guarantee that at the moment its NOT running
    	else {
    		hasStarted = true;
    		start();
    	}
    }
    

    /**
     * This method schedules a new task (a child of the current task) to the
     * same processor which currently handles this task.
     *
     * @param task the task to execute
     */
    protected final void spawn(Task<?>... task) {
    	// adds every task to the processor
        for(Task<?> t : task){
        	p.submit(t);
        }
    }

    /**
     * add a callback to be executed once *all* the given tasks results are
     * resolved
     *
     * Implementors note: make sure that the callback is running only once when
     * all the given tasks completed.
     *
     * @param tasks
     * @param callback the callback to execute once all the results are resolved
     */
    protected final void whenResolved(Collection<? extends Task<?>> tasks, Runnable callback) {
    	
    	check = new Checker(this, p.getPool());
    	
    	for( Task<?> t : tasks){
    		check.inc();
    	}
    	
    	for(Task<?> tt : tasks){
    		tt.getResult().whenResolved(()->{
    			check.dec();
    		});
    	}
    	
    	done = callback;
        
    }

    /**
     * resolve the internal result - should be called by the task derivative
     * once it is done.
     * resolve() is synchronized
     *
     * @param result - the task calculated result
     * 
     */
    protected final void complete(R result) {
        d.resolve(result);
    }

    /**
     * @return this task deferred result.
     */
    public final Deferred<R> getResult() {
        return d;
    }

}
