package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;

import java.util.SplittableRandom;

/**
 * R2D2Microservices is in charge of the handling {@link DeactivationEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link DeactivationEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class R2D2Microservice extends MicroService {

    private long DeactivationDuration;
    private final Callback<DeactivationEvent> deactivationEventCallback = (DeactivationEvent d)->{
        try {Thread.sleep(DeactivationDuration);}
        catch (InterruptedException i){}
        diary.R2D2Deactivate = System.currentTimeMillis();
        System.out.println("deactivation done");
        sendEvent(new BombDestroyerEvent());
    };
    private Callback<TerminationBroadcast> terminationBroadcastCallback = (TerminationBroadcast)->{
        terminate();
        diary.R2D2Terminate = System.currentTimeMillis();
    };

    public R2D2Microservice(long duration) {
        super("R2D2");
        this.DeactivationDuration = duration;
    }

    public long getDeactivationDuration(){
        return DeactivationDuration;
    }
    @Override
    protected void initialize() {
        subscribeEvent(DeactivationEvent.class, deactivationEventCallback);
        subscribeBroadcast(TerminationBroadcast.class, terminationBroadcastCallback);
    }
}
