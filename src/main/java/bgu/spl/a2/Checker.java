package bgu.spl.a2;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class aim to count how much subtasks are waiting to be resolved.
 * when all the subtasks are resolved, the father tasks is getting back 
 * to the pool in order to be resolved.
 */

public class Checker {
	/**
	 * @param father - the task that waits for the subtasks to be resolved.
	 */
private Task<?> father = null;

/**
 * @param size - how much subtasks we need to be resolved.
 */
private AtomicInteger size = new AtomicInteger(0);

/**
 * refernce to the original pool where all the tasks belongs to.
 */
private WorkStealingThreadPool pool;


/**
 * constructor
 * @param f - father of all the subtasks, the task need all the subtasks
 * to be resolved.
 * @param p - original pool of threads (processors)
 */
public Checker(Task<?> f, WorkStealingThreadPool p){
	father = f;
	pool = p;
}

/**
 * when another subtask is being created, if the father task
 * is need to wait in order to the subtask to be resolved,
 * the subtasks increases the number of subtasks that needs to be resolved.
 */
void inc(){
	size.incrementAndGet();
}

/**
 * when a subtask is resolved, it decreases the number of subtasks that needs to be resolved.
 */
void dec(){
	size.decrementAndGet();
	if(size.get() == 0){
		pool.submit(father);
	}
}

}
