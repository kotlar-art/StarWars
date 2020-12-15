package bgu.spl.mics.application.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Future;
import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.passiveObjects.Diary;



/**
 * LeiaMicroservices Initialized with Attack objects, and sends them as  {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LeiaMicroservice extends MicroService {

    private Attack[] attacks;
    private Future[] onGoingAttacks;
    private final Callback<BeginEvent> beginEventCallback = (BeginEvent be)->{
        for (int attackEventRunnner = 0; attackEventRunnner<attacks.length; attackEventRunnner++){
            AttackEvent currAttack = new AttackEvent(attacks[attackEventRunnner]);
            onGoingAttacks[attackEventRunnner] = sendEvent(currAttack);
        }
        int futureRunner = 0;
        while (futureRunner<onGoingAttacks.length){
            onGoingAttacks[futureRunner].get();
            futureRunner++;
        }
        DeactivationEvent deactivationEvent = new DeactivationEvent();
        sendEvent(deactivationEvent);
        return;

    };
    private final Callback<TerminationBroadcast> terminationCallback = (TerminationBroadcast b)->{
        this.terminate();
        diary.LeiaTerminate = System.currentTimeMillis();
    };
	
    public LeiaMicroservice(Attack[] attacks) {
        super("Leia");
		this.attacks = attacks;
		onGoingAttacks = new Future[attacks.length];
    }

    @Override
    protected void initialize() {
        subscribeEvent(BeginEvent.class, beginEventCallback);
    	subscribeBroadcast(TerminationBroadcast.class, terminationCallback);
    }

}
