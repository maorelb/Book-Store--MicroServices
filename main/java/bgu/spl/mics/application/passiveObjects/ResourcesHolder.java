package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing the resource manager.
 * You must not alter any of the given public methods of this class.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class ResourcesHolder {

    private LinkedBlockingQueue<DeliveryVehicle> freeVehicles;
    private LinkedBlockingQueue<Future> futureVehicles;

	/**
	 * Retrieves the single instance of this class.
	 */

	private static class ResourcesHolderSingleton{
		private static ResourcesHolder singleton = new ResourcesHolder();
	}
	/**
	 * Retrieves the single instance of this class.
	 */
	public static ResourcesHolder getInstance() {
		return ResourcesHolderSingleton.singleton;
	}

	public ResourcesHolder(){
	    freeVehicles=new LinkedBlockingQueue<>();
	    futureVehicles=new LinkedBlockingQueue<>();
    }

	/**
	 * Tries to acquire a vehicle and gives a future object which will
	 * resolve to a vehicle.
	 * <p>
	 * @return 	{@link Future<DeliveryVehicle>} object which will resolve to a
	 * 			{@link DeliveryVehicle} when completed.
	 */
	public synchronized Future<DeliveryVehicle> acquireVehicle() {
	    Future<DeliveryVehicle> f=new Future<>();
	    if(!freeVehicles.isEmpty())
	        f.resolve(freeVehicles.poll());
	    else
            futureVehicles.add(f);
	    return f;
	}

	/**
	 * Releases a specified vehicle, opening it again for the possibility of
	 * acquisition.
	 * <p>
	 * @param vehicle	{@link DeliveryVehicle} to be released.
	 */
	public synchronized void releaseVehicle(DeliveryVehicle vehicle) {

		if(vehicle==null) {
			//Cancelling Drives after receiving termination tick
			for (Future<DeliveryVehicle> f : futureVehicles) {
				f.resolve(null);
			}
			return;
		}

	    if(futureVehicles.isEmpty())
	        freeVehicles.add(vehicle);
	    else
	        futureVehicles.poll().resolve(vehicle);
	}

	/**
	 * Receives a collection of vehicles and stores them.
	 * <p>
	 * @param vehicles	Array of {@link DeliveryVehicle} instances to store.
	 */
	public void load(DeliveryVehicle[] vehicles) {

		for(DeliveryVehicle dv: vehicles)
			freeVehicles.add(dv);
	}

	public String toString(){
		return freeVehicles.toString();
	}


}