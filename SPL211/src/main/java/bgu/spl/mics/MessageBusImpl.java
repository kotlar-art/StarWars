package bgu.spl.mics;
import java.util.*;
import java.util.concurrent.*;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.BombDestroyerEvent;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private ConcurrentHashMap<MicroService, ConcurrentLinkedQueue<Message>> MessageQueues;
	private ConcurrentHashMap<Class<? extends Event>, Queue<MicroService>> EventSubscribers;
	private ConcurrentHashMap<Class<? extends Broadcast>, List<MicroService>> BroadcastSubscribers;
	private ConcurrentHashMap<Event, Future> OnGoingEvents;
	public ConcurrentHashMap getEventsubscribers(){ //only for debugging!!!!!!
		return EventSubscribers;
	}
	private static class SingletonHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	public static MessageBusImpl getInstance(){
		return SingletonHolder.instance;
	}

	private MessageBusImpl(){
		MessageQueues = new ConcurrentHashMap<MicroService, ConcurrentLinkedQueue<Message>>();
		EventSubscribers = new ConcurrentHashMap<Class<? extends Event>, Queue<MicroService>>();
		BroadcastSubscribers = new ConcurrentHashMap<Class<? extends Broadcast>, List<MicroService>>();
		OnGoingEvents = new ConcurrentHashMap<Event, Future>();
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {

		if (!EventSubscribers.containsKey(type)){
			synchronized (EventSubscribers) {
				if (!EventSubscribers.containsKey(type)) {
					EventSubscribers.put(type, new ConcurrentLinkedQueue<MicroService>());
					if (type.equals(AttackEvent.class)) System.out.println(m.getName() + " just created Attackeventsubscribers queue");
				}

			}
		}
		EventSubscribers.get(type).add(m);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if (!BroadcastSubscribers.contains(type)) {
			synchronized ((BroadcastSubscribers)) {
				if (!BroadcastSubscribers.contains(type))
					BroadcastSubscribers.put(type, new LinkedList<MicroService>());
			}
		}
		System.out.println(Thread.currentThread().getName());
		BroadcastSubscribers.get(type).add(m);
    }

	@Override @SuppressWarnings("unchecked")
	public <T> void complete(Event<T> e, T result) {
		OnGoingEvents.get(e).resolve(result);
		synchronized (OnGoingEvents.get(e)) {
			OnGoingEvents.get(e).notifyAll();
		}
		OnGoingEvents.remove(e);
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		int i = 0;
		boolean needToAlert = false;
		List<MicroService> subscribers = BroadcastSubscribers.get(b.getClass());
		while (i<subscribers.size()){
			MicroService receiver = subscribers.get(i);
			Queue<Message> receiverQ = MessageQueues.get(receiver);
			if (receiverQ.isEmpty()) needToAlert = true;
			receiverQ.add(b);
			synchronized (receiverQ) {
				if (needToAlert) receiverQ.notify();
			}
			i++;
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		Future<T> f = new Future<>();
		boolean needToAlert = false;
		OnGoingEvents.put(e, f);
		Queue<MicroService> receiversQ = EventSubscribers.get(e.getClass());
		if (e.getClass().equals(AttackEvent.class)) System.out.println("queue's head is before remove is " + receiversQ.peek().getName());
		System.out.println("just send an event of type " + e.getClass());
		MicroService receiver = receiversQ.remove();
		receiversQ.add(receiver);
		if (e.getClass().equals(AttackEvent.class)) System.out.println("queue's head after remove is " + receiversQ.peek().getName());
		ConcurrentLinkedQueue<Message> receiverQ = MessageQueues.get(receiver);
		if (receiverQ.isEmpty()) needToAlert = true;
		MessageQueues.get(receiver).add(e);

		if (needToAlert) {
			synchronized (receiverQ) {
				receiverQ.notify();
			}
		}
		return f;
	}

	@Override
	public void register(MicroService m) {
		ConcurrentLinkedQueue<Message> q = new ConcurrentLinkedQueue<>();
		MessageQueues.put(m, q);
	}

	@Override
	public void unregister(MicroService m) {
		MessageQueues.remove(m);
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		Message message = null;
		ConcurrentLinkedQueue<Message> q = MessageQueues.get(m);
		if (q.isEmpty()) {
			synchronized (q) {
				if (q.isEmpty()) {
					try {
						q.wait();
					} catch (InterruptedException i) {
					}
				}
			}
		}
		message = MessageQueues.get(m).remove();
		return message;
	}
}
