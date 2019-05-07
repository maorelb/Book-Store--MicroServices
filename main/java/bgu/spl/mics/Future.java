package bgu.spl.mics;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
	private T result;
	private CountDownLatch count;
	private boolean isDone;
	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {
		//TODO: implement this
		count = new CountDownLatch(1);
		isDone=false;
		result = null;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     * 	       
     */
	public T get() {
		//TODO: implement this.

		if(!isDone()) {
			try {
				count.await();
			} catch (InterruptedException e) {
				System.out.println("countdown latch error");
				e.printStackTrace();
			}
		}
			return result;
	}
	
	/**
     * Resolves the result of this Future object.
     */
	public void resolve (T result) {
		//TODO: implement this.
		this.result = result;
		count.countDown();
		isDone=true;
	}
	
	/**
     * @return true if this object has been resolved, false otherwise
     */
	public boolean isDone() {
		//TODO: implement this.
		return isDone;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */
	public T get(long timeout, TimeUnit unit) {
		//TODO: implement this.
		if(isDone())
			return result;
		try {
			Thread.sleep(unit.toMillis(timeout));
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		// After waiting the required time future object is still not resolved
		if(!this.isDone())
			return null;
		return result;
	}

}
