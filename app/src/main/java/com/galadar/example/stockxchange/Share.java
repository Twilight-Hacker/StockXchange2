package com.galadar.example.stockxchange;

/**
 * Created by Galadar on 1/10/2015.
 * Object for holding shares data
 */
public class Share {

    int sid;                //The id used to recognise the share. This is equal to the index in the Finance array,
    // but must be retrievable from here as well
    int currentSharePrice;  //The price of this share at this time
    int prevDayClose;       //The price the share closed at the previous day, for showing in Share Info Activity
    String name;            //The name of the share that is also the name of the company
    int total;              //The total amount of shares this company has available.

    public Share(String name, int sid, int currentSharePrice, int total) {
        this.name = name;
        this.sid = sid;
        this.currentSharePrice = currentSharePrice;
        this.prevDayClose = currentSharePrice;
        this.total = total;
    }

    public int getId() {
        return sid;
    }

    public String getName() {
        return name;
    }

    public int getPrevDayClose() {
        return prevDayClose;
    }

    public int getCurrentSharePrice() {
        return currentSharePrice;
    }

    public int getTotalShares() {
        return total;
    }
}
