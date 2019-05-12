import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class WebFrame extends JFrame{
	private DefaultTableModel model;
	
	
	public WebFrame() {
		JComponent content = (JComponent)getContentPane();
		content.setLayout(new BorderLayout());
		
		model = new DefaultTableModel(new String[] { "url", "status"}, 0);  
		JTable tables = new JTable(model);
		tables.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane scrol = new JScrollPane(tables);
		scrol.setPreferredSize(new Dimension(600,300)); 
		content.add(BorderLayout.NORTH, scrol);
		
		JPanel down = new JPanel();
		down.setLayout(new BoxLayout(down, BoxLayout.Y_AXIS));
		JButton single = new JButton("Concurrent Fetch");
		JButton concurrent = new JButton("Single Thread Fetch");
		JTextField threadCount = new JTextField(5);
		threadCount.setMaximumSize(threadCount.getPreferredSize());
		JLabel running = new JLabel("Running:0");
		JLabel completed = new JLabel("Completed:0");
		JLabel elapsed = new JLabel("Elapsed:");
		JProgressBar progress = new JProgressBar();
		JButton stop = new JButton("stop");
		
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
	
	
	
	
	
	
	
	
	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) { }
		
		WebFrame frame = new WebFrame();
		frame.setSize(600, 500);
		
		
		
	}
	
}
