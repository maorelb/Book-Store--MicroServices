package bgu.spl.mics.application.services;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Pair;
import bgu.spl.mics.application.messages.BookOrderEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.OrderReceipt;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * APIService is in charge of the connection between a client and the store.
 * It informs the store about desired purchases using {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

public class APIService extends MicroService {

//orderSchedule contains pairs of <bookTitle, tick>
	ArrayList<Pair<String,Integer>> orderSchedule;
	private int currentTick;
	private Customer c;
	private static int serviceCounter=1;
	private CountDownLatch countdown;


	public APIService(ArrayList<Pair<String,Integer>> orderSchedule,Customer c, CountDownLatch countdown) {
		super("APIService"+serviceCounter);
		this.orderSchedule=orderSchedule;
		this.c=c;
		serviceCounter++;
		this.countdown = countdown;
	}
	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, (TickBroadcast m) -> {
			currentTick = m.getTick();
			Pair order;
				while(!orderSchedule.isEmpty()&& currentTick==orderSchedule.get(0).getRight()) {
					order = orderSchedule.remove(0);
					Future<OrderReceipt> f=sendEvent(new BookOrderEvent((String) order.getLeft(),c,(int) order.getRight()));
					if(f!= null && f.get()!=null)
						c.addReceipt(f.get());
				}
		});
		subscribeBroadcast(TerminationBroadcast.class, (TerminationBroadcast broadcast) -> {
			terminate();
		});
		countdown.countDown();
	}




}