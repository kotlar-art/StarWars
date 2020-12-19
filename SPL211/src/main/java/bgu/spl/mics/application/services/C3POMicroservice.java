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

    private Ewoks ewoks;//C3PO must have access to Ewoks for the attacks
    private final Callback<AttackEvent> attackCallback = (AttackEvent AE)->{
        Attack a = AE.getAttack();
        int[] required = new int[a.getEwoks().size()];
        for (int i = 0; i<a.getEwoks().size(); i++){//he gets the information about the required ewoks for the attack
            required[i] = a.getEwoks().get(i);
        }
        Arrays.sort(required);//this so Han and C3PO don't find themselves in a deadlock. they will ask for ewoks in the same order
        ewoks.acquire(required);
        try {
            Thread.sleep(a.howLong());//he attacks
        }
        catch (InterruptedException i){}
        ewoks.release(required);
        this.complete(AE, true);
        diary.C3POFinish = System.currentTimeMillis();
        diary.totalAttacks.incrementAndGet();
    };
    private final Callback terminationCallback = (T)->{
        C3POMicroservice.this.terminate();
        diary.C3POTerminate = System.currentTimeMillis();
    };

    public C3POMicroservice() {
        super("C3PO");
        this.ewoks = Ewoks.getInstance();
    }

    public C3POMicroservice(String name) {
        super(name);
        this.ewoks = Ewoks.getInstance();
    }


    @Override
    protected void initialize() {
        this.subscribeEvent(AttackEvent.class, attackCallback);
        this.subscribeBroadcast(TerminationBroadcast.class, terminationCallback);
    }
}
