package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AcquireVehicleEvent;
import bgu.spl.mics.application.messages.DeliveryEvent;
import bgu.spl.mics.application.messages.ReleaseVehicleEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Logistic service in charge of delivering books that have been purchased to customers.
 * Handles {@link DeliveryEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LogisticsService extends MicroService {
	private static int serviceCounter=1;
	private CountDownLatch countdown;

	public LogisticsService(CountDownLatch countdown) {
		super("LogisticsService#"+serviceCounter);
		serviceCounter++;
		this.countdown = countdown;
	}

	@Override
	protected void initialize() {
		subscribeEvent(DeliveryEvent.class, (DeliveryEvent e)->{
			Customer c = e.getCustomer();
			Future<Future<DeliveryVehicle>> f=sendEvent(new AcquireVehicleEvent());
			if(f==null ||f.get()==null)
			{
				complete(e,null);
				return;
			}
			DeliveryVehicle v = f.get().get();
				//
			if(v==null){
				complete(e,null);
				return;}

			v.deliver(c.getAddress(), c.getDistance());
			sendEvent(new ReleaseVehicleEvent(v));
		});
		subscribeBroadcast(TerminationBroadcast.class, (TerminationBroadcast broadcast) -> {
			terminate();

		});
		countdown.countDown();
		
	}
	public String toString(){return getName();}

}
