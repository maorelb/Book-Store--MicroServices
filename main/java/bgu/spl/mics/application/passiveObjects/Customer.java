package bgu.spl.mics.application.passiveObjects;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Passive data-object representing a customer of the store.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class Customer implements Serializable {
	private int id;
	private String name;
	private String address;
	private int distance;
	private int creditNumber;
	private int creditAmount;
	private List<OrderReceipt> receipts;

	public Customer(int id,String name, String address, int distance, int creditNumber,int creditAmount)
	{
		this.id=id;
		this.name = name;
		this.address = address;
		this.distance = distance;
		this.creditNumber=creditNumber;
		this.creditAmount=creditAmount;
		receipts=new ArrayList<>();
	}

	/**
     * Retrieves the name of the customer.
     */
	public String getName() {
		return name;
	}

	/**
     * Retrieves the ID of the customer  . 
     */
	public int getId() {
		return id;
	}
	
	/**
     * Retrieves the address of the customer.  
     */
	public String getAddress() {
		return address;
	}
	
	/**
     * Retrieves the distance of the customer from the store.  
     */
	public int getDistance() {
		return distance;
	}

	
	/**
     * Retrieves a list of receipts for the purchases this customer has made.
     * <p>
     * @return A list of receipts.
     */
	public List<OrderReceipt> getCustomerReceiptList() {
		return receipts;
	}
	
	/**
     * Retrieves the amount of money left on this customers credit card.
     * <p>
     * @return Amount of money left.   
     */
	public int getAvailableCreditAmount() {
		return creditAmount;
	}

	/**
     * Retrieves this customers credit card serial number.    
     */
	public int getCreditNumber() {
		return creditNumber ;
	}

	public synchronized void chargeCustomer(int amount)
	{
			creditAmount-=amount;

	}

	public void addReceipt(OrderReceipt r){
		receipts.add(r);
	}
}
