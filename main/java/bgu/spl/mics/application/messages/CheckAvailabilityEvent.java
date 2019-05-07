package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;


public class CheckAvailabilityEvent implements Event<Integer> {

    private String bookTitle;
    public CheckAvailabilityEvent(String name){
        bookTitle = name;
    }
    public String getBookTitle() {
         return bookTitle;
    }
    public String toString(){return "Check availability Event for " + bookTitle;}
}
