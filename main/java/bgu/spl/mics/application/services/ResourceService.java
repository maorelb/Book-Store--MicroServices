package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AcquireVehicleEvent;
import bgu.spl.mics.application.messages.DeliveryEvent;
import bgu.spl.mics.application.messages.ReleaseVehicleEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;

import java.util.concurrent.CountDownLatch;

/**
 * ResourceService is in charge of the store resources - the delivery vehicles.
 * Holds a reference to the {@link ResourceHolder} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ResourceService extends MicroService {
    private static int count=1;
    private ResourcesHolder resourcesHolder;
    private CountDownLatch countdown;

	public ResourceService(CountDownLatch countdown) {
        super("ResourceService#" + count);
        count++;
        resourcesHolder=ResourcesHolder.getInstance();
        this.countdown = countdown;

	}

	protected void initialize() {
	    subscribeEvent(AcquireVehicleEvent.class, (AcquireVehicleEvent event)-> {
	        Future<DeliveryVehicle> f=resourcesHolder.acquireVehicle();
	        complete(event, f);
        }  );
		subscribeBroadcast(TerminationBroadcast.class, (TerminationBroadcast broadcast) -> {
			resourcesHolder.releaseVehicle(null);
			terminate();
		});

		subscribeEvent(ReleaseVehicleEvent.class, (ReleaseVehicleEvent event)->{
				resourcesHolder.releaseVehicle(event.getVehicle());
		});
		countdown.countDown();
	}

	public String toString(){return resourcesHolder.toString();}
}
