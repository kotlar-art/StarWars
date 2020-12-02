package bgu.spl.mics;
import java.util.*;
import java.util.concurrent.*;

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
	private static MessageBusImpl instance = null;

	public static MessageBusImpl getInstance(){
		if (instance==null){
			synchronized (instance.getClass()){
				if(instance==null){
					instance = new MessageBusImpl();
				}
			}
		}
		return instance;
	}

	private MessageBusImpl(){
		MessageQueues = new ConcurrentHashMap<MicroService, ConcurrentLinkedQueue<Message>>();
		EventSubscribers = new ConcurrentHashMap<Class<? extends Event>, Queue<MicroService>>();
		BroadcastSubscribers = new ConcurrentHashMap<Class<? extends Broadcast>, List<MicroService>>();
		OnGoingEvents = new ConcurrentHashMap<Event, Future>();
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if (!EventSubscribers.contains(type)){
			EventSubscribers.put(type, new ConcurrentLinkedQueue<MicroService>());
		}
		EventSubscribers.get(type).add(m);

	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if (!BroadcastSubscribers.contains(type)) {
			BroadcastSubscribers.put(type, new LinkedList<MicroService>());
		}
		BroadcastSubscribers.get(type).add(m);
    }

	@Override @SuppressWarnings("unchecked")
	public <T> void complete(Event<T> e, T result) {
		OnGoingEvents.get(e).resolve(result);
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
			if(needToAlert) receiverQ.notify();
			i++;
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		Future<T> f = new Future<>();
		boolean needToAlert = false;
		OnGoingEvents.put(e, f);
		Queue<MicroService> q = EventSubscribers.get(e.getClass());
		MicroService receiver = q.remove();
		q.add(receiver);
		ConcurrentLinkedQueue<Message> receiverQ = MessageQueues.get(receiver);
		if (receiverQ.isEmpty()) needToAlert = true;
		MessageQueues.get(receiver).add(e);
		if (needToAlert) receiverQ.notify();
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
		if (q.isEmpty()){
			try {q.wait();}
			catch (InterruptedException i) {}
		}
		message = MessageQueues.get(m).remove();
		return message;
	}
}
