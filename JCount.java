import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class JCount extends JPanel{
	
	// variables for class
	private JTextField till;
	private JLabel curr;
	private JButton start;
	private JButton stop;
	private Worker worker;
	private final int defMax = 100000000;
	private final int regInterval = 100;
	
	// constructs new JCount object
	public JCount() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		till = new JTextField(15);
		curr = new JLabel("0");
		start = new JButton("Start");
		stop = new JButton("Stop");
		
		this.add(till);
		this.add(curr);
		this.add(start);
		this.add(stop);
		addListeners();
	}
	
	// adds listeners to start and stop buttons
	private void addListeners() {
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(worker != null && worker.isAlive()) {
					worker.interrupt();
				}
				int max;
				try {
					max = Integer.parseInt(till.getText());
				} catch (NumberFormatException num) {
					max = defMax;
				}
				worker = new Worker(max);
				worker.start();
			}
		});
		
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(worker != null && worker.isAlive()) {
					worker.interrupt();
				}
			}
		});
	}

	// creates and shows GUI
	private static void createAndShowGUI(int nOfWorkers) {
		JFrame countFrame = new JFrame();
		countFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		countFrame.getContentPane().setLayout(new BoxLayout(countFrame.getContentPane(), BoxLayout.Y_AXIS));;
		for(int i = 0; i < nOfWorkers; i++) {
			JCount section = new JCount();
			countFrame.add(section);
		}
		countFrame.pack();
		countFrame.setVisible(true);
	}

	// extends Thread class to count till max
	public class Worker extends Thread {
		private int max;
		
		public Worker(int max) {
			this.max = max;
		}
		
		@Override
		public void run() {
			for(int i = 0; i <= max; i++) {
				if(isInterrupted()) {
					break;
				}
				int cu = i;
				if(i % 10000 == 0) {
					try {
						Worker.sleep(regInterval);
					} catch (InterruptedException e) {
						break;
					}
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							curr.setText("" + cu);
						}
					});
				}
				
			}
		}
		
	}
		 
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
		public void run() {   
			createAndShowGUI(4);  
		}
	 });
	} 
	
}
