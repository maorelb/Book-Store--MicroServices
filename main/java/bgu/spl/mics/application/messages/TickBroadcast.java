package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast{
    private int currentTick;
    public TickBroadcast(int tick)
    {
        currentTick = tick;
    }
    public int getTick(){ return currentTick;}


    public String toString(){return "tickBroadCast:" + currentTick;}
}
