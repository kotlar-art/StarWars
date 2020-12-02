package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;


/**
 * C3POMicroservices is in charge of the handling {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class C3POMicroservice extends MicroService {

    private Integer FavoriteNumber;//for testing purposes
	
    public C3POMicroservice() {
        super("C3PO");
    }
    public C3POMicroservice(Integer o){
        super("C3PO");
        FavoriteNumber = o;
    }

    @Override
    protected void initialize() {

    }
    public void increment(){
        FavoriteNumber++;
    }

    public Integer getFavoriteNumber(){
        return FavoriteNumber;
    }
}
