package bgu.spl.mics;

import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.services.C3POMicroservice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageBusImplTest {

    private MessageBusImpl MB;
    private C3POMicroservice MS;

    @BeforeEach
    void setUp() {
        MB = MessageBusImpl.getInstance();
        MS = new C3POMicroservice();
    }

    @Test
    void testSubscribeEvent() {//this code tests subscribeEvent, register, sendEvent, and awaitMessage all together
        AttackEvent AE = new AttackEvent();
        MB.register(MS);
        MS.subscribeEvent(AE.getClass(), (o)->{});
        Message m = null;
        MB.sendEvent(AE);
        try {
            m = MB.awaitMessage(MS);
        }
        catch (InterruptedException i){};
        assertEquals(AE,m);
    }

    @Test
    void testSubscribeBroadcast() { //also tests sendBroadcast
        TerminationBroadcast TB = new TerminationBroadcast();
        MB.register(MS);
        MS.subscribeBroadcast(TB.getClass(), (TerminationBroadcast)->{});
        MB.sendBroadcast(TB);
        Message M = null;
        try{
            M = MB.awaitMessage(MS);
        }
        catch (InterruptedException i){}
        assertEquals(M,TB);
    }

    @Test
    void testComplete() {
        MB.register(MS);
        AttackEvent AE = new AttackEvent();
        MS.subscribeEvent(AE.getClass(),(AttackEvent)->{});
        C3POMicroservice Sender = new C3POMicroservice();
        Future<Boolean> f = Sender.sendEvent(AE);
        Message received = null;
        try {
            received = MB.awaitMessage(MS);
        }
        catch (InterruptedException i){};
        MS.complete((Event<Boolean>)received, true);
        assertTrue(f.isDone());
        assertEquals(f.get(),true);
    }
}