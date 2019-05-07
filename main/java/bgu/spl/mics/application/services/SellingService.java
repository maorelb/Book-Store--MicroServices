package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.*;

import java.util.concurrent.CountDownLatch;

/**
 * Selling service in charge of taking orders from customers.
 * Holds a reference to the {@link MoneyRegister} singleton of the store.
 * Handles {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class SellingService extends MicroService {
	private MoneyRegister safe;
	private int currentTick;
	private static int count=1;
	CountDownLatch countdown;

	public SellingService(CountDownLatch countdown) {
		super("SellingService#"+count);
		count++;
		safe = MoneyRegister.getInstance();
		this.countdown = countdown;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, (TickBroadcast event) -> {
			currentTick = event.getTick();
		});

		subscribeBroadcast(TerminationBroadcast.class, (TerminationBroadcast broadcast) -> {
			terminate();
		});

		subscribeEvent(BookOrderEvent.class, (BookOrderEvent event) -> {
			int orderTick = event.getOrderTick();
			int processTick = currentTick;
			Customer c = event.getCustomer();
			String bookName = event.getBookName();

			Future<Integer> f = sendEvent(new CheckAvailabilityEvent(bookName));
			if(f==null || f.get()== null)
			{
				complete(event,null);
				return;
			}
			int bookPrice = f.get();
			//book not exist
			if (bookPrice == -1) {
				complete(event, null);
				return;
			}
				//customer has enough money
			 if (c.getAvailableCreditAmount() >= bookPrice) {
				synchronized (c) { // no one else charge the customer - to make sure has enough money
					Future<OrderResult> f2 = sendEvent(new TakeBookEvent(bookName));
					OrderResult res = f2.get();
					if (res == OrderResult.NOT_IN_STOCK) {
						complete(event, null);
						return;
					}
					safe.chargeCreditCard(c, bookPrice);
				}
				OrderReceipt r = new OrderReceipt(getName(), c.getId(), bookName, bookPrice, orderTick, processTick, currentTick);
				safe.file(r);
				complete(event, r);
				// Book sold - delivery request has been sent
				sendEvent(new DeliveryEvent(c));
			 } else {
			 	complete(event, null);
			 }
		});
		countdown.countDown();


	}
}