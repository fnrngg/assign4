import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class WebWorker extends Thread {
	private String urlString;
	private int row;
	private WebFrame frame;
	private long start;
	private long end;
	private String status = "";
	
	public WebWorker(String url, int row, WebFrame frame) {
		urlString = url;
		this.row = row;
		this.frame = frame;
	}

	// downloads url content and adds status for each url
	public void download() {
		start = System.currentTimeMillis();
 		InputStream input = null;
		StringBuilder contents = null;
		try {
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
		
			// Set connect() to throw an IOException
			// if connection does not succeed in this many msecs.
			connection.setConnectTimeout(5000);
			
			connection.connect();
			input = connection.getInputStream();

			BufferedReader reader  = new BufferedReader(new InputStreamReader(input));
		
			char[] array = new char[1000];
			int len;
			contents = new StringBuilder(1000);
			while ((len = reader.read(array, 0, array.length)) > 0) {
				if(isInterrupted()) {
					throw new InterruptedException();
				}
				contents.append(array, 0, len);
				Thread.sleep(100);
			}
			end = System.currentTimeMillis();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			status = "" + sdf.format(new Date(start)) + " " + (end - start) + "ms " + contents.length() + "bytes";
			
		}
		// Otherwise control jumps to a catch...
		catch(MalformedURLException ignored) {
			status = "err";
		}
		catch(InterruptedException exception) {
			status = "interrupted";
		}
		catch(IOException ignored) {
			status = "err";
		}
		// "finally" clause, to close the input stream
		// in any case
		finally {
			try{
				if (input != null) input.close();
			}
			catch(IOException ignored) {}
		}
	}

	@Override
	public void run() {
		download();
		frame.addStatus(row, status);
	}
	
}
