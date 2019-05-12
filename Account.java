
public class Account {

	private int id;
	private int balance;
	private int trans;
	
	/*
	 * Creates new account and sets its id balance and default number of transactions
	 * 
	 */
	public Account(int id, int balance) {
		this.id = id;
		this.balance = balance;
		this.trans = 0;
	}
	
	/*
	 * Returns balance of this account
	 */
	public synchronized int getBalance() {
		return balance;
	}
	
	/*
	 * Updates account balance and number of transactions
	 */
	public synchronized void updateBalance(int newBalance) {
		balance += newBalance;
		trans++;
	}
	
	@Override
	public String toString() {
		String res = "acct:" + id + " bal:" + balance + " trans" + trans;
		return res;
	}
	
}
