package bgu.spl.mics.application.passiveObjects;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

/**
 * Passive object representing the resource manager.
 * <p>
 * This class must be implemented as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class Ewoks {

    private Vector<Ewok> ewoks; //list of the ewoks available
    private Vector<Object> locks; // lock is created for each ewok, for sync purposes
    private static class SingletonHolder {
        private static Ewoks instance = new Ewoks();
    }

    public static Ewoks getInstance(){
        return SingletonHolder.instance;
    }

    private Ewoks(){ }

    public void setEwoks(int ewoksSupplied){
        ewoks = new Vector<Ewok>(ewoksSupplied + 1);
        locks = new Vector<Object>(ewoksSupplied + 1);
        ewoks.add(null);
        locks.add(null);
        for (int i = 1; i<=ewoksSupplied; i++){
            Ewok newEwok = new Ewok(i);
            ewoks.add(i, newEwok);
            locks.add(i, new Object());
        }
    }

    public void acquire(int[] required){
        for (int i = 0; i<required.length; i++){
            Object currentLock = locks.get(required[i]); //choosing the compatible lock for the ewok required
            synchronized (currentLock){
                Ewok curr = ewoks.get(required[i]);
                while (!curr.getAvailability()){
                    try {
                        currentLock.wait(); //waiting until this certain ewok is available
                    }
                    catch (InterruptedException IE){};
                }
                curr.acquire();
            }
        }
    }

    public void release(int[] usedEwoks){
        for (int i = 0; i<usedEwoks.length; i++){
            int toRelease = usedEwoks[i];
            ewoks.get(toRelease).release();
            synchronized (locks.get(toRelease)) { //releasing the ewoks and notifying on each of their lock so if anyone is waiting he can get hold of them
                locks.get(toRelease).notify();
            }
        }
    }

}
