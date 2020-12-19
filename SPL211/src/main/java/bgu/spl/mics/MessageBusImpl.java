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

	private ConcurrentHashMap<MicroService, ConcurrentLinkedQueue<Message>> MessageQueues; //will keep each microservice's message queues
	private ConcurrentHashMap<Class<? extends Event>, LinkedBlockingQueue<MicroService>> EventSubscribers; //will keep which microserivce registered to which event
	private ConcurrentHashMap<Class<? extends Broadcast>, Vector<MicroService>> BroadcastSubscribers; //will keep which microserivce registered to which broadcast
	private ConcurrentHashMap<Event, Future> OnGoingEvents; //will connect each event with the compatible future
	private ConcurrentHashMap<MicroService, LinkedList<Class<? extends Event>>> UnregistermapE; //reverse microservice-event map
	private ConcurrentHashMap<MicroService, LinkedList<Class<? extends Broadcast>>> UnregistermapB; //reverse microservice-broadcast map

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
			EventSubscribers.putIfAbsent(type, new LinkedBlockingQueue<MicroService>()); //creating a new queue for the event if not existed
			EventSubscribers.get(type).add(m); //registering to an event
			UnregistermapE.putIfAbsent(m, new LinkedList<Class<? extends Event>>());
			UnregistermapE.get(m).add(type);
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized (BroadcastSubscribers) {
			BroadcastSubscribers.putIfAbsent(type, new Vector<MicroService>()); //creating a new list for the broadcast if not existed
			BroadcastSubscribers.get(type).add(m); //registering to the broadcast
			UnregistermapB.putIfAbsent(m, new LinkedList<Class<? extends Broadcast>>());
			UnregistermapB.get(m).add(type);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> void complete(Event<T> e, T result) {
		OnGoingEvents.get(e).resolve(result); //resolving the future
		synchronized (OnGoingEvents.get(e)) {
			OnGoingEvents.get(e).notifyAll(); //if someone is waiting in the event to see if it's resolved, notifying it
		}
		OnGoingEvents.remove(e);
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		int i = 0;
		boolean needToAlert = false;
		List<MicroService> subscribers = BroadcastSubscribers.get(b.getClass());
		synchronized (BroadcastSubscribers) { //syncing because when unregistrating we can get outofbound exception
			while (i < subscribers.size()) {
				MicroService receiver = subscribers.get(i); //getting the microservice from the broadcast list
				Queue<Message> receiverQ = MessageQueues.get(receiver);
				if (receiverQ.isEmpty()) {
					needToAlert = true;
				}
				receiverQ.add(b); //adding the broadcast to his message queue

				synchronized (receiverQ) {
					if (needToAlert) {
						receiverQ.notify(); //notifying the microservice if he is waiting for a message
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
		OnGoingEvents.put(e, f); //adding the future to the list
		LinkedBlockingQueue<MicroService> receiversQ = EventSubscribers.get(e.getClass());
		if (receiversQ == null || receiversQ.isEmpty()) {
			return null;
		}
		MicroService receiver;
		synchronized (EventSubscribers) { //have to syncronise to preserve round robin
			receiver = receiversQ.remove();
			receiversQ.add(receiver);
		}
		ConcurrentLinkedQueue<Message> receiverQ = MessageQueues.get(receiver);
		if (receiverQ.isEmpty()) needToAlert = true;
		MessageQueues.get(receiver).add(e); //adding the even to the microservice's message queue

		if (needToAlert) {
			synchronized (receiverQ) {
				receiverQ.notify(); //notifying the microservice if he is waiting for a message
			}
		}
		return f;
	}


	@Override
	public void register(MicroService m) {
		ConcurrentLinkedQueue<Message> q = new ConcurrentLinkedQueue<>();
		MessageQueues.put(m, q); //creating a message queue for the microservice
	}

	@Override
	public void unregister(MicroService m) {
		synchronized (EventSubscribers) {
			if (UnregistermapE.get(m) != null) {
				for (Class<? extends Event> event : UnregistermapE.get(m)) {
					EventSubscribers.get(event).remove(m); //removing the microservice from all his event queues
				}
			}
		}
		synchronized (BroadcastSubscribers) {
			if (UnregistermapB.get(m) != null) {
				for (Class<? extends Broadcast> broadcast : UnregistermapB.get(m)) {
					{
						BroadcastSubscribers.get(broadcast).remove(m); //removing the microservice from all his broadcast lists
					}
				}
			}
		}
			UnregistermapB.remove(m);
			UnregistermapE.remove(m);
			MessageQueues.remove(m); //removing his message queue

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		Message message = null;
		ConcurrentLinkedQueue<Message> q = MessageQueues.get(m);
		if (q.isEmpty()) {
			synchronized (q) {
				if (q.isEmpty()) {
					try {
						q.wait(); //if the queue is empty, waiting for a notify on the message queue. notification happens either on send event or send broadcast
					} catch (InterruptedException i) {
					}
				}
			}
		}
		message = MessageQueues.get(m).remove();
		return message;
	}
}
