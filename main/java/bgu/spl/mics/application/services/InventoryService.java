package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CheckAvailabilityEvent;
import bgu.spl.mics.application.messages.TakeBookEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.passiveObjects.BookInventoryInfo;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.OrderResult;

import java.util.concurrent.CountDownLatch;

/**
 * InventoryService is in charge of the book inventory and stock.
 * Holds a reference to the {@link Inventory} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

public class InventoryService extends MicroService {
	private static int count=1;
	private Inventory library;
	private CountDownLatch countdown;

	public InventoryService(CountDownLatch countdown) {
		super("inventoryService#"+count);
		count++;
		library = Inventory.getInstance();
		this.countdown = countdown;
	}
	@Override
	protected void initialize() {
		subscribeEvent(CheckAvailabilityEvent.class, (CheckAvailabilityEvent e)->{
			int price = library.checkAvailabiltyAndGetPrice(e.getBookTitle());
			complete(e,price);

		});

		subscribeEvent(TakeBookEvent.class, (TakeBookEvent e)->{
			OrderResult res = library.take(e.getBookTitle());
			complete(e,res);

		});
		subscribeBroadcast(TerminationBroadcast.class, (TerminationBroadcast broadcast) -> {
			terminate();
		});
		countdown.countDown();

	}

	public String toString () {
		return getName();
	}

}
