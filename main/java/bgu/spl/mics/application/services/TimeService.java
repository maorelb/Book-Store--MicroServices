package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.MoneyRegister;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;

import java.util.Timer;
import java.util.TimerTask;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

public class TimeService extends MicroService {

	private int duration;
	private int speed;
	private Timer timer;
	private int numOfTicks;

	public TimeService(int speed, int duration) {
		super("time1");
		timer=new Timer();
		this.duration = duration;
		this.speed = speed;
		numOfTicks=1;
	}

	protected void initialize(){

		subscribeBroadcast(TerminationBroadcast.class, (tbc) -> terminate());

		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				if (numOfTicks == duration) {
					sendBroadcast(new TerminationBroadcast());
					timer.cancel();
				} else {
					sendBroadcast(new TickBroadcast(numOfTicks));
					numOfTicks++;
				}
			}
		};
	    timer.scheduleAtFixedRate(timerTask,10,speed);
    }

	public String toString(){return "Timer speed is "+ speed + " duration:"+duration;}

}
