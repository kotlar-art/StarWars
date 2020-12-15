package bgu.spl.mics.application.passiveObjects;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive data-object representing a Diary - in which the flow of the battle is recorded.
 * We are going to compare your recordings with the expected recordings, and make sure that your output makes sense.
 * <p>
 * Do not add to this class nothing but a single constructor, getters and setters.
 */
public class Diary {


    public AtomicInteger getNumberOfAttacks() {
        return totalAttacks;
    }

    public long getC3POFinish() {
        return C3POFinish;
    }

    public long getHanSoloFinish() {
        return HanSoloFinish;
    }

    public long getR2D2Deactivate() {
        return R2D2Deactivate;
    }

    public void resetNumberAttacks() {
        totalAttacks.set(0);
    }

    private static class SingletonHolder {
        private static Diary instance = new Diary();
    }
    public AtomicInteger totalAttacks = new AtomicInteger(0);
    public long HanSoloFinish;
    public long C3POFinish;
    public long R2D2Deactivate;
    public long LeiaTerminate;
    public long HanSoloTerminate;
    public long C3POTerminate;
    public long R2D2Terminate;
    public long LandoTerminate;


    public static Diary getInstance(){
        return SingletonHolder.instance;
    }

    private Diary(){
    }

    public long getR2D2Terminate() {
        return R2D2Terminate;
    }
    public long getLandoTerminate() {
        return  LandoTerminate;
    }
    public long getC3POTerminate() {
        return C3POTerminate;
    }

    public long getHanSoloTerminate() {
        return HanSoloTerminate;
    }


    //debug
    @Override
    public String toString() {
        String string1 = "totalAttacks:" + totalAttacks + "\n" + "HanSoloFinish:" + HanSoloFinish + "\n";
        String string2 = "C3POFinish:" + C3POFinish + "\n" + "R2D2Deactivate:" + R2D2Deactivate + "\n";
        String string3 = "LeiaTerminate:" + LeiaTerminate + "\n" + "HanSoloTerminate:" + HanSoloTerminate + "\n";
        String string4 = "C3POTerminate:" + C3POTerminate + "\n" + "R2D2Terminate" + R2D2Terminate + "\n" + "LandoTerminate" + LandoTerminate + "\n";
        return string1 + string2 + string3 + string4;
    }
}
