import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class WebFrame extends JFrame{
	private DefaultTableModel model;
	private JButton single;
	private JButton concurrent;
	private JButton stop;
	private JTable table;
	private JTextField threadCount;
	private WebFrame frame = this;
	private Launcher launcher;
	private Semaphore maxPerTime;
	private CountDownLatch finish;
	private JLabel running;
	private JLabel completed;
	private JLabel elapsed;
	private long start;
	private JProgressBar progress;
	private AtomicInteger comp;
	private AtomicInteger runningThreadsCount;
	
	public WebFrame(String fileName) {
		JComponent content = (JComponent)getContentPane();
		content.setLayout(new BorderLayout());
		
		model = new DefaultTableModel(new String[] { "url", "status"}, 0);  
		table = new JTable(model);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane scrol = new JScrollPane(table);
		scrol.setPreferredSize(new Dimension(600,300)); 
		content.add(BorderLayout.NORTH, scrol);
		
		JPanel down = new JPanel();
		down.setLayout(new BoxLayout(down, BoxLayout.Y_AXIS));
		single = new JButton("Single Thread Fetch");
		concurrent = new JButton("Concurrent Fetch");
		threadCount = new JTextField(5);
		threadCount.setMaximumSize(threadCount.getPreferredSize());
		running = new JLabel("Running:0");
		completed = new JLabel("Completed:0");
		elapsed = new JLabel("Elapsed:");
		stop = new JButton("stop");
		stop.setEnabled(false);
		addListeners();
		addUrls(fileName);
		progress = new JProgressBar(0, model.getRowCount());
		progress.setStringPainted(true);
		
		
		down.add(single);
		down.add(concurrent);
		down.add(threadCount);
		down.add(running);
		down.add(completed);
		down.add(elapsed);
		down.add(progress);
		down.add(stop);
		
		content.add(BorderLayout.SOUTH, down);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	
	// called by webWorker
	// adds status for each url
	public void addStatus(int row, String status) {
		model.setValueAt(status, row, 1);
		completed.setText("completed:" + comp.incrementAndGet());
		running.setText("running:" + runningThreadsCount.decrementAndGet());
		progress.setValue(comp.get());
		maxPerTime.release();
		finish.countDown();
	}
	
	// adds urls to table
	private void addUrls(String fileName) {
		try {
			Scanner scanner = new Scanner(new FileReader(fileName));
			while(scanner.hasNextLine()) {
				model.addRow(new String[] {scanner.nextLine(), ""});
			}
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	// updates buttons and labels
	private void stopFetching(long elapsed) {
		if(launcher.isAlive()) {
			synchronized (launcher) {
				launcher.interrupt();
			}
			double seconds = elapsed / 1000D;
			this.elapsed.setText("elapsed:" + seconds);
			concurrent.setEnabled(true);
			single.setEnabled(true);
			stop.setEnabled(false);
		}
	}

	// changes all statuses to ""
	private void clearStatuses() {
		for (int i = 0; i < model.getRowCount(); i++) {
			model.setValueAt("", i, 1);
		}
	}
	
	// initializes variables for launcher and updates buttons
	private void startRunning(int max) {
		clearStatuses();
		maxPerTime = new Semaphore(max);
		finish = new CountDownLatch(model.getRowCount());
		launcher = new Launcher(frame, maxPerTime, finish);
		launcher.start();
		progress.setValue(0);
		
		single.setEnabled(false);
		concurrent.setEnabled(false);
		stop.setEnabled(true);
	}
	
	// adds listeners for buttons
	private void addListeners() {
		single.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				startRunning(1);
			}
		});
		concurrent.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					startRunning(Integer.parseInt(threadCount.getText()));
				} catch (NumberFormatException num) {
					startRunning(1);
				}
			}
		});
		stop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				stopFetching(System.currentTimeMillis() - start);
			}
		});
	}

// launcher class
// creates webWorker threads and runs
public class Launcher extends Thread {
	private Semaphore maxPerTime;
	private WebFrame frame;
	private List<WebWorker> workers;
	private CountDownLatch finish;
	
	// constructor
	public Launcher(WebFrame frame, Semaphore maxPerTime, CountDownLatch finish) {
		this.maxPerTime = maxPerTime;
		this.frame = frame;
		this.finish = finish;
	}
	
	// creates threads and waits until all threads are done 
	// and then calls stopFetching
	// if interrupted interrupts all running threads
	@Override
	public void run() {
		runningThreadsCount = new AtomicInteger(1);
		workers = new ArrayList<WebWorker>();
		start = System.currentTimeMillis();
		elapsed.setText("elapsed:");
		completed.setText("completed: 0");
		comp = new AtomicInteger(0);
		
		try {
			for(int i = 0; i < model.getRowCount(); i++) {
				maxPerTime.acquire();
				synchronized (launcher) {
					if(isInterrupted()) {
						workers.get(i).interrupt();
						break;
					}
					workers.add(new WebWorker((String)model.getValueAt(i, 0), i, frame));
					workers.get(i).start();
				}
				
				running.setText("running:" + runningThreadsCount.incrementAndGet());
			}
			finish.await();
			running.setText("running:" + runningThreadsCount.decrementAndGet());
			stopFetching(System.currentTimeMillis() - start);
		} catch (InterruptedException e) {
			for (int j = 0; j < workers.size(); j++) {
				if(workers.get(j).isAlive()) {
					workers.get(j).interrupt();
				}
			}
			running.setText("running:" + runningThreadsCount.decrementAndGet());
		}
	}
	
	
}





	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) { }
		
		WebFrame frame = new WebFrame("links.txt");
		frame.setSize(600, 500);
		
		
		
	}
	
}
