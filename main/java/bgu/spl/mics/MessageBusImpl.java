package bgu.spl.mics;

import java.util.concurrent.*;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {


	private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> mapServiceToQ;
	private ConcurrentHashMap<Class<? extends Event>, LinkedBlockingQueue<MicroService>> mapEventToServices;
	private ConcurrentHashMap<Class<? extends Broadcast>, LinkedBlockingQueue<MicroService>> mapBroadcastToServices;
	private ConcurrentHashMap<Event, Future> mapEventToFuture;

	private static class MessageBusHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}


	private MessageBusImpl() {

		mapServiceToQ = new ConcurrentHashMap<>();
		mapEventToServices = new ConcurrentHashMap<>();
		mapBroadcastToServices = new ConcurrentHashMap<>();
		mapEventToFuture = new ConcurrentHashMap<>();
	}

	public static MessageBusImpl getInstance() {
		return MessageBusHolder.instance;
	}

	@Override
	public void register(MicroService m) {
		mapServiceToQ.put(m, new LinkedBlockingQueue<>());
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {

		Message msg = mapServiceToQ.get(m).take();
		return msg;

	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if (!mapEventToServices.containsKey(type)) {
			mapEventToServices.put(type, new LinkedBlockingQueue<>());
		}
		mapEventToServices.get(type).add(m);
	}


		@Override
		public void subscribeBroadcast (Class < ? extends Broadcast > type, MicroService m){
			if (!mapBroadcastToServices.containsKey(type))
				mapBroadcastToServices.put(type, new LinkedBlockingQueue<>());
			mapBroadcastToServices.get(type).add(m);
		}

		@Override
		public void sendBroadcast (Broadcast b){

			if(!mapBroadcastToServices.containsKey(b.getClass())&& !mapBroadcastToServices.get(b.getClass()).isEmpty())
				mapBroadcastToServices.put(b.getClass(),new LinkedBlockingQueue<>());

			else{
				for (MicroService m : mapBroadcastToServices.get(b.getClass()))
					if (mapServiceToQ.get(m) != null)
						mapServiceToQ.get(m).add(b);
			}
		}

		@Override
		public <T> Future<T> sendEvent(Event <T> e) {
			//round robin matter
			MicroService m=null;

			//null returned if no service that can handle the event exists
			if(mapEventToServices.containsKey(e.getClass()) && !mapEventToServices.get(e.getClass()).isEmpty())
				 m = mapEventToServices.get(e.getClass()).poll();
			else
				{
					mapEventToServices.put(e.getClass(), new LinkedBlockingQueue<>());
					return null;
				}

			if (m != null) {
				Future<T> f = new Future<>();
				mapEventToFuture.put(e,f);
				// inserting to the message queue
				if(mapServiceToQ.get(m)!=null)
					mapServiceToQ.get(m).offer(e);
				// adding the microservice to the end of the event queue
				mapEventToServices.get(e.getClass()).add(m);
				return f;
			}
			else{
				return null;
			}

		}
		@Override
		public <T > void complete (Event < T > e, T result){
			try {
				Future <T> f = mapEventToFuture.get(e);
				f.resolve(result);
			} catch (Exception exp) {

				exp.printStackTrace();
			}

		}

		@Override
		public void unregister (MicroService m){
//		 deletes all occurence of service from brodcast's subscribers list
			for(LinkedBlockingQueue<MicroService> q: mapBroadcastToServices.values())
				q.remove(m);
//		 deletes all occurences of service from event's subscribers list
		for(LinkedBlockingQueue queue: mapEventToServices.values())
			queue.remove(m);

		LinkedBlockingQueue queue = mapServiceToQ.get(m);
		if(!queue.isEmpty())
		{
			for(Object message : queue){
				if(message instanceof Event){
					Event e = (Event) message;
					complete(e, null);
				}
				queue.remove(message);
			}
		}
		mapServiceToQ.remove(m);
		}

	}

