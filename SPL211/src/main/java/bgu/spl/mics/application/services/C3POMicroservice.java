package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.passiveObjects.Ewoks;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import java.util.Arrays;

/**
 * C3POMicroservices is in charge of the handling {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class C3POMicroservice extends MicroService {

    private Ewoks ewoks;
    private final Callback<AttackEvent> attackCallback = (AttackEvent AE)->{
        Attack a = AE.getAttack();
        int[] required = new int[a.getEwoks().size()];
        for (int i = 0; i<a.getEwoks().size(); i++){
            required[i] = a.getEwoks().get(i);
        }
        Arrays.sort(required);
        ewoks.acquire(required);
        try {
            Thread.sleep(a.howLong());
        }
        catch (InterruptedException i){}
        ewoks.release(required);
        this.complete(AE, true);
        System.out.println(getName() + " attack finish");
        diary.C3POFinish = System.currentTimeMillis();
        diary.totalAttacks.incrementAndGet();
    };
    private final Callback terminationCallback = (T)->{
        C3POMicroservice.this.terminate();
        diary.C3POTerminate = System.currentTimeMillis();
    };

    public C3POMicroservice(Ewoks ewoks) {
        super("C3PO");
        this.ewoks = ewoks;
    }

    public C3POMicroservice(){
        super("C3PO");
    }

    @Override
    protected void initialize() {
        this.subscribeEvent(AttackEvent.class, attackCallback);
        this.subscribeBroadcast(TerminationBroadcast.class, terminationCallback);
    }
}
