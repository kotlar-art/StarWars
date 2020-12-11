package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;

/**
 * LandoMicroservice
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LandoMicroservice  extends MicroService {

    private long duration;
    private final Callback<TerminationBroadcast> terminationBroadcastCallback = (TerminationBroadcast t)->{
        terminate();
        diary.LandoTerminate = System.currentTimeMillis();
    };
    private final Callback<BombDestroyerEvent> bombDestroyerEventCallback = (BombDestroyerEvent b)->{
        try {Thread.sleep(LandoMicroservice.this.getDuration());}
        catch (InterruptedException i) {}
        complete(b, true);
        System.out.println(" Lando is doing the bomb ");
        sendBroadcast(new TerminationBroadcast());
    };

    public LandoMicroservice(long duration) {
        super("Lando");
        this.duration = duration;
    }

    public long getDuration(){
        return duration;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminationBroadcast.class, terminationBroadcastCallback);
        subscribeEvent(BombDestroyerEvent.class, bombDestroyerEventCallback);
    }
}
