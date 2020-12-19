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
	private ConcurrentHashMap<Class<? extends Event>, LinkedBlockingQueue<MicroService>> EventSubscribers;
	private ConcurrentHashMap<Class<? extends Broadcast>, Vector<MicroService>> BroadcastSubscribers;
	private ConcurrentHashMap<Event, Future> OnGoingEvents;
	private ConcurrentHashMap<MicroService, LinkedList<Class<? extends Event>>> UnregistermapE;
	private ConcurrentHashMap<MicroService, LinkedList<Class<? extends Broadcast>>> UnregistermapB;

	private static class SingletonHolder {
		private static MessageBusImpl instance = new MessageBusImpl();

		private static void reset() {
			instance = new MessageBusImpl();
		}
	}


	public static MessageBusImpl getInstance() {
		return SingletonHolder.instance;
	}

	public static void reset() {
		SingletonHolder.reset();
	}

	private MessageBusImpl() {
		MessageQueues = new ConcurrentHashMap<MicroService, ConcurrentLinkedQueue<Message>>();
		EventSubscribers = new ConcurrentHashMap<Class<? extends Event>, LinkedBlockingQueue<MicroService>>();
		BroadcastSubscribers = new ConcurrentHashMap<Class<? extends Broadcast>, Vector<MicroService>>();
		OnGoingEvents = new ConcurrentHashMap<Event, Future>();
		UnregistermapE = new ConcurrentHashMap<MicroService, LinkedList<Class<? extends Event>>>();
		UnregistermapB = new ConcurrentHashMap<MicroService, LinkedList<Class<? extends Broadcast>>>();
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		synchronized (EventSubscribers) {
			EventSubscribers.putIfAbsent(type, new LinkedBlockingQueue<MicroService>());
			EventSubscribers.get(type).add(m);
			UnregistermapE.putIfAbsent(m, new LinkedList<Class<? extends Event>>());
			UnregistermapE.get(m).add(type);
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized (BroadcastSubscribers) {
			BroadcastSubscribers.putIfAbsent(type, new Vector<MicroService>());
			BroadcastSubscribers.get(type).add(m);
			UnregistermapB.putIfAbsent(m, new LinkedList<Class<? extends Broadcast>>());
			UnregistermapB.get(m).add(type);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
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
		synchronized (BroadcastSubscribers) {
			while (i < subscribers.size()) {
				MicroService receiver = subscribers.get(i);
				Queue<Message> receiverQ = MessageQueues.get(receiver);
				if (receiverQ.isEmpty()) {
					needToAlert = true;
				}
				receiverQ.add(b);

				synchronized (receiverQ) {
					if (needToAlert) {
						receiverQ.notify();
					}
				}
				i++;
			}
		}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		Future<T> f = new Future<>();
		boolean needToAlert = false;
		OnGoingEvents.put(e, f);
		LinkedBlockingQueue<MicroService> receiversQ = EventSubscribers.get(e.getClass());
		if (receiversQ == null || receiversQ.isEmpty()) {
			return null;
		}
		MicroService receiver;
		synchronized (EventSubscribers) {
			receiver = receiversQ.remove();
			receiversQ.add(receiver);
		}
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
		synchronized (EventSubscribers) {
			if (UnregistermapE.get(m) != null) {
				for (Class<? extends Event> event : UnregistermapE.get(m)) {
					EventSubscribers.get(event).remove(m);
				}
			}
		}
		synchronized (BroadcastSubscribers) {
			if (UnregistermapB.get(m) != null) {
				for (Class<? extends Broadcast> broadcast : UnregistermapB.get(m)) {
					{
						BroadcastSubscribers.get(broadcast).remove(m);
					}
				}
			}
		}
			UnregistermapB.remove(m);
			UnregistermapE.remove(m);
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
