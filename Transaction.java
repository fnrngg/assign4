
public class Transaction {
	private final int from;
	private final int to;
	private final int amount;
	
	/*
	 * Constructor of transaction
	 */
	public Transaction(int from, int to, int amount) {
		this.from = from;
		this.to = to;
		this.amount = amount;
	}
	
	/*
	 * Returns account id from which money is transfered
	 */
	public int getFrom() {
		return from;
	}
	
	/*
	 * Returns account id to which money is transfered
	 */
	public int getTo() {
		return to;
	}
	
	/*
	 * Returns amount of money transfered
	 */
	public int getAmount() {
		return amount;
	}
	
	/*
	 * Returns true if both Transactions are the same
	 */
	public boolean equals(Transaction tr) {
		if(tr.amount == this.amount && tr.from == this.from && tr.to == this.to) {
			return true;
		} return false;
	}
	
}
