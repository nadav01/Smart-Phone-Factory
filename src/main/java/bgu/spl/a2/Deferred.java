package bgu.spl.a2;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean; 

/**
 * this class represents a deferred result i.e., an object that eventually will
 * be resolved to hold a result of some operation, the class allows for getting
 * the result once it is available and registering a callback that will be
 * called once the result is available.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 *
 * @param <T> the result type
 */
public class Deferred<T> {
	
	/**
	 * @param callbacks - the list of callbacks should be triggered 
	 */
	private ConcurrentLinkedQueue<Runnable> callbacks = new ConcurrentLinkedQueue<>();
	
	/**
	 * @param result - the final result of the task. Initialized as null.
	 */
	private T result = null;
	
	/**
	 * @param isResolved - indicates if the resolve() has been called
	 */
	private AtomicBoolean isResolved = new AtomicBoolean(false);
	
	/**
	 * @param check - checks if all the subtasks are resolved (if needed)
	 */
	private Checker check;

    /**
     *
     * @return the resolved value if such exists (i.e., if this object has been
     * {@link #resolve(java.lang.Object)}ed yet
     * @throws IllegalStateException in the case where this method is called and
     * this object is not yet resolved
     * 
     * @pre: isResolved == true
     * @post: none
     * @inv: none
     */
    public T get() {
        if(isResolved.get() == false){
        throw new IllegalStateException("This object is not yet resolved.");
        }
        else return result;
    }

    /**
     *
     * @return true if this object has been resolved - i.e., if the method
     * {@link #resolve(java.lang.Object)} has been called on this object before.
     * @pre: none
     * @post: none
     * @inv: none
     */
    public boolean isResolved() {
        return isResolved.get();
    }

    /**
     * resolve this deferred object - from now on, any call to the method
     * {@link #get()} should return the given value
     *
     * Any callbacks that were registered to be notified when this object is
     * resolved via the {@link #whenResolved(java.lang.Runnable)} method should
     * be executed before this method returns
     * 
     * This method is synchronized because we don't want other threads to interfere
     * while resolving this object. We don't want one thread to call resolve()
     * while other calls whenResolved().
     *
     * @param value - the value to resolve this deferred object with
     * @throws IllegalStateException in the case where this object is already
     * resolved
     * 
     * @pre: value != null & !isResolved & result == null
     * @post: result != null & isResolved & callbacks.size()==@PRE (callbacks.size())+1
     * @inv : callbacks.size() >= 0
     */
    public synchronized void resolve(T value) {
        if(isResolved.get()) throw new IllegalStateException("This object is already resolved.");
        if(value == null) throw new NullPointerException("The result can not be null.");
        else{
        	result = value;
        	isResolved.compareAndSet(false, true); // changing isResolved from false to true
        	
        	//execute every callback in the list and then removes it (as requested)
        	while(!callbacks.isEmpty()){
        		Runnable r = callbacks.poll();
        		r.run();
        	}
        }
    }

    /**
     * add a callback to be called when this object is resolved. if while
     * calling this method the object is already resolved - the callback should
     * be called immediately
     *
     * Note that in any case, the given callback should never get called more
     * than once, in addition, in order to avoid memory leaks - once the
     * callback got called, this object should not hold its reference any
     * longer.
     *
     *This method is synchronized because we want to avoid a situation when
     *while calling whenResolve() other thread can call resolve() may end up
     *not executing a callback.
     *
     * @param callback the callback to be called when the deferred object is
     * resolved
     * 
     * @pre: callback != null
     * @post: isResoved || callbacks.contains(callback)
     * @inv: @post (callbacks.size()) >= @pre (callbacks.size())
     */
    public synchronized  void whenResolved(Runnable callback) {
        if(callback == null) throw new NullPointerException("The callback can not be null.");
        if(isResolved.get()) callback.run();
        else callbacks.add(callback);
    }
    
    /* package */ void setChecker(Checker c){
    	check = c;
    }

}
