import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Cracker {
	// Array of chars used to produce strings
	public static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789.,-!".toCharArray();	
	
	/*
	 Given a byte[] array, produces a hex String,
	 such as "234a6f". with 2 chars for each byte in the array.
	 (provided code)
	*/
	public static String hexToString(byte[] bytes) {
		StringBuffer buff = new StringBuffer();
		for (int i=0; i<bytes.length; i++) {
			int val = bytes[i];
			val = val & 0xff;  // remove higher bits, sign
			if (val<16) buff.append('0'); // leading 0
			buff.append(Integer.toString(val, 16));
		}
		return buff.toString();
	}
	
	/*
	 Given a string of hex byte values such as "24a26f", creates
	 a byte[] array of those values, one byte value -128..127
	 for each 2 chars.
	 (provided code)
	*/
	public static byte[] hexToArray(String hex) {
		byte[] result = new byte[hex.length()/2];
		for (int i=0; i<hex.length(); i+=2) {
			result[i/2] = (byte) Integer.parseInt(hex.substring(i, i+2), 16);
		}
		return result;
	}
	
	// possible test values:
	// a 86f7e437faa5a7fce15d1ddcb9eaeaea377667b8
	// fm adeb6f2a18fe33af368d91b09587b68e3abcb9a7
	// a! 34800e15707fae815d7c90d49de44aca97e2d759
	// xyz 66b27417d37e024c46526c2f6d358a754fc552f3
	
	// generates hash for a string entered by user
	public String generateHash(String name) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(name.getBytes());
			return hexToString(md.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String cracked;
	private final int maxThreads = 40;
	
	// creates workers to perform a brute force attack and finds original String
	public String crackHash(String hash, String length, String nOfWorkers) {
		int len = Integer.parseInt(length);
		int numWorkers = Integer.parseInt(nOfWorkers);
		if(numWorkers > maxThreads) {
			return null;
		}
		int divided = 40 / numWorkers;
		int curr = 0;
		CountDownLatch cracking = new CountDownLatch(1);
		List<Worker> workers = new ArrayList<Worker>();
		Worker worker;
		for(int i = 0; i < numWorkers - 1; i++) {
			worker = new Worker(curr, curr + divided - 1, len, hash, cracking);
			workers.add(worker);
			worker.start();
			curr += divided;
		}
		worker = new Worker(curr, 39, len, hash, cracking);
		worker.start();
		workers.add(worker);
		
		try {
			cracking.await();
			for(int i = 0; i < numWorkers; i++) {
				workers.get(i).interrupt();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return cracked;
	}
	
	
	
	// worker class
	// generates all possible Strings to find original one
	public class Worker extends Thread {
		private int from, to, len;
		private String hash;
		private CountDownLatch cracking;
		
		public Worker(int from, int to, int len, String hash, CountDownLatch cracking) {
			this.from = from;
			this.to = to;
			this.len = len;
			this.hash = hash;
			this.cracking = cracking;
		}
		
		// finds original string with length less or equal to len
		private void recFind(String next) {
			if(next.length() == len) {
				return;
			}
			for (int i = 0; i < CHARS.length; i++) {
				if(isInterrupted()) {
					return;
				}
				if(generateHash(next + CHARS[i]).equals(hash)) {
					cracked = next + CHARS[i];
					cracking.countDown();
				} else {
					recFind(next + CHARS[i]);
				}
			}
		}
		
		@Override
		public void run() {
			for(int i = from; i < to; i++) {
				if(isInterrupted()) {
					return;
				}
				String next = "" + CHARS[i];
				if(generateHash(next).equals(hash)) {
					cracked = next;
					cracking.countDown();
				}
				recFind(next);
			}
		}
		
	}
	
	
	
	public static void main(String args[]) {
		Cracker crack = new Cracker();
		
		if(args.length == 1) {
			System.out.println(crack.generateHash(args[0]));
		} else if(args.length == 3) {
			System.out.println(crack.crackHash(args[0], args[1], args[2]));
		} else {
			String molly = crack.generateHash("molly");
			System.out.println(molly);
			System.out.println(crack.crackHash(molly, "5", "10"));
		}
	}

}
