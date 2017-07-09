package com.galadar.example.stockxchange;

/**
 * Created by Galadar on 5/11/2015.
 * Event Object
 *
 * This is a wrapper object for Randomly Generated Events. These in general affect Sector outlooks and even the economy as a whole.
 */
public class Event {
    private int type;       //Each event has a type that determines what it affects.
    private int magnitude;  //Magnitude determines how big is the effect of the event
    private int duration;   //How many days will the event be active. This is also affected/determined by magnitude.

    public Event(int type, int magnitude) {
        this.type = type;
        this.magnitude = magnitude;
        this.duration = 2*(int)Math.round((double)magnitude/10);
    }

    public Event(int type, int magnitude, int duration) {
        this.type = type;
        this.magnitude = magnitude;
        this.duration = duration;
    }

    public int getType() {
        return type;
    }

    public int getMagnitude() {
        return magnitude;
    }

    public int getDuration() {
        return duration;
    }

    public void dayEnded(){
        this.duration--;
    }

    public boolean eventEnded(){
        return this.duration==0;
    }


}
