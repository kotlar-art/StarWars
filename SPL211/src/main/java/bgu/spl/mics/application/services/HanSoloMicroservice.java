package bgu.spl.mics.application.services;
import bgu.spl.mics.Callback;
import bgu.spl.mics.Event;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.passiveObjects.Attack;
import java.util.List;
import bgu.spl.mics.application.passiveObjects.Ewoks;
import java.util.Arrays;

/**
 * HanSoloMicroservices is in charge of the handling {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class HanSoloMicroservice extends MicroService {

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
        diary.HanSoloFinish = System.currentTimeMillis();
        diary.totalAttacks.incrementAndGet();
    };
    private final Callback<TerminationBroadcast> terminateCallback = (T)->{
        HanSoloMicroservice.this.terminate();
        diary.HanSoloTerminate = System.currentTimeMillis();
    };

    public HanSoloMicroservice(Ewoks ewoks) {
        super("Han");
        this.ewoks = ewoks;
    }

    @Override
    protected void initialize() {
        this.subscribeEvent(AttackEvent.class, attackCallback);
        this.subscribeBroadcast(TerminationBroadcast.class, terminateCallback);
    }
}
