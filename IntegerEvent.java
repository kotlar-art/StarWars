package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class IntegerEvent implements Event<Integer> {

    private Integer i;

    public IntegerEvent(Integer j){
        i=j;
    }
    public void setT(Integer j){
        i=j;
    }
}
