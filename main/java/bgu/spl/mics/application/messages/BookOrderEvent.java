package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.OrderReceipt;


public class BookOrderEvent implements Event<OrderReceipt> {
    private String bookTitle;
    private Customer c;
    private int orderTick;

    public BookOrderEvent(String bookName, Customer c, int orderTick)
    {
        this.bookTitle=bookName;
        this.c=c;
        this.orderTick = orderTick;
    }
    public Customer getCustomer()
    {
        return c;
    }

    public String getBookName() {
        return bookTitle;
    }
    public int getOrderTick(){
        return orderTick;
    }

    public String toString(){return "Book Order Event for "+ c.getName() +" " + bookTitle + " at tick " + orderTick;}
}
