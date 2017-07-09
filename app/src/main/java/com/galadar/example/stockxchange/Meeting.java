package com.galadar.example.stockxchange;

import java.util.ArrayList;

/**
 * Created by Galadar on 28/10/2015.
 * Meeting Object
 *
 * Meetings act as tutorials, teaching both game operations and real financial knowledge on how the (stock) markets work.
 *
 * Meetings are stored in an xml in recourse folder.
 */
public class Meeting {

    private int day;                    //The day that the meeting will be shown to the player
    private String title;               //The title of the meeting
    private ArrayList<String> speech;   //The text of the meeting

    public Meeting(int day, String title, ArrayList<String> speech) {
        this.day=day;
        this.title = title;
        this.speech = speech;
    }

    public int getDay() {
        return this.day;
    }

    public String getMeetingTitle() {
        return this.title;
    }

    public ArrayList<String> getMeetingSpeech() {
        return this.speech;
    }
}
