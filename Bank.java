import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Bank {
	private List<Account> accs;
	private BlockingQueue<Transaction> trans;
	private int numofAccs = 20;
	private int numOfWorkers;
	private final Transaction nullTrans = new Transaction(-1,0,0); 
	
	/*
	 * Constructs new bank
	 */
	public Bank(int numOfWorkers) {
		this.numOfWorkers = numOfWorkers;
		accs = new ArrayList<Account>();
		trans = new ArrayBlockingQueue<Transaction>(numOfWorkers);
		for(int i = 0; i < numofAccs; i++) {
			accs.add(new Account(i, 1000));
		}
	}
	
	/*
	 * Prints final account values for all accounts
	 */
	public void printAccValues() {
		for(int i = 0; i < accs.size(); i++) {
			System.out.println(accs.get(i));
		}
	}
	
	/*
	 * Inits and starts worker threads
	 */
	public void startWorkers() {
		for (int i = 0; i < numOfWorkers; i++) {
			Worker newOne = new Worker();
			newOne.start();
		}
	}
	
	/*
	 * Reads and puts transactions from file to blockingQueue
	 * size of blockingQueue is equal to number of workers so 
	 * when printAccValues is called all transactions are already made and it 
	 * doesn't need to use countDownLacht
	 */
	private void readAndAdd(String fileName) {
		try {
			BufferedReader read = new BufferedReader(new FileReader(fileName));
			StreamTokenizer tokens = new StreamTokenizer(read);
			
			while(tokens.nextToken() != StreamTokenizer.TT_EOF) {
				int from = (int)tokens.nval;
				tokens.nextToken();
				int to = (int)tokens.nval;
				tokens.nextToken();
				int amount = (int)tokens.nval;
				Transaction newTrans = new Transaction(from, to, amount);
				trans.put(newTrans);
			}
			for(int i = 0; i < numOfWorkers; i++) {
				trans.put(nullTrans);
			}
			read.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		printAccValues();
	}
	
	public class Worker extends Thread {
		public void run() {
			try {
				while(true) {
						Transaction newTrans = trans.take();
						if(newTrans.equals(nullTrans)) {
							break;
						}
						Account from = accs.get(newTrans.getFrom());
						Account to = accs.get(newTrans.getTo());
//						int differenceOfFrom = from.getBalance() - newTrans.getAmount();
//						if(differenceOfFrom < 0) {
//							System.out.println("Not enought money");
//						}
						if(newTrans.getFrom() < newTrans.getTo()) {
							from.updateBalance(-newTrans.getAmount());
							to.updateBalance(newTrans.getAmount());
						} else {
							to.updateBalance(newTrans.getAmount());
							from.updateBalance(-newTrans.getAmount());
						}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String args[]) {
		Bank bank = new Bank(20);
		bank.startWorkers();
		bank.readAndAdd("100k.txt");
		
	}
}


