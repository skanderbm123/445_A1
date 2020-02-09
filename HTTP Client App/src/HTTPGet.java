import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;

public class HTTPGet {

	String url;
	
	public HTTPGet() {
		System.out.println("--GET REQUEST--");
		String url;
	}

	public void setUrl(String newUrl) {
		url = newUrl;
	}
	public String getUrl() {
		return url;
	}
	
	public void operation(String[] args) {

		if(!url.equals("")) {
			try {				
				// To get IP address of URL
			
				InetAddress ip = InetAddress.getByName(new URL(url).getHost());
				
				// Checks if given URL is good or not
				if(!ip.isReachable(0))
					System.out.println("URL provided is not a valid one!");
				
				Socket socket = new Socket(ip,80);
			
			
				// Setting up input and output streams
				InputStream inputStream = socket.getInputStream();
				OutputStream outputStream = socket.getOutputStream();
				
				int KeysValue = GetArgs(args);
				String body;
				
				if(KeysValue==-1) {
					body="";
				}else {
					body=args[args.length-1].substring(KeysValue+1);
				}
	
				String request = "GET /get?"+body+" HTTP/1.1\r\nhost:"+ip.getHostName()+"\r\n\r\n";
				
				outputStream.write(request.getBytes());
				outputStream.flush();
				
				StringBuilder response = new StringBuilder();
				
				int responseData = inputStream.read();
				int redirect_Counter = 0;
				
				// Convert response bytes to String
				while(responseData != -1) {
					response.append((char) responseData);
					responseData = inputStream.read();
					//System.out.println(response);
				}
				
				// Check whether 'verbose' option was passed in command arguments
				CheckOptions(args , response);
			
				
				socket.close();
				inputStream.close();
				outputStream.close();
			}
			catch(Exception e) {
				System.err.println(e);
			}
		}
		else {
			System.out.println("Please enter valid arguments of the form:\n" + 
					"httpc (get|post) [-v] (-h \"k:v\")* [-d inline-data] [-f file] URL");
		}
	}

	// Check for verbose and options commands (-v, -o)
	private void CheckOptions(String[] args , StringBuilder response) {
		boolean verbose = false;
		boolean options = false;
		String outputTxtFile = "";
		
		for(int i=0 ; i < args.length ; i++) {
			if(args[i].equals("-v"))
				verbose = true;
			else if(args[i].equals("-o")) {
				options = true;
			}
			
			if(options == true) {
				outputTxtFile = args[i+1];
				break;
			}			
		}	
		
		PrintWriter pw = null;
		File file = null;
		
		if(options) {
		try {
			file = new File(outputTxtFile);
			boolean created = file.createNewFile();
			pw = new PrintWriter(new FileOutputStream(outputTxtFile));
		}
		catch(FileNotFoundException e) {
			System.out.println("File " + outputTxtFile + " not found.");
		}
		catch(IOException e) {
			System.out.println("File not created");
		}
		}
		
		if(options && verbose) {
			pw.println("Server response: " + response);
			pw.close();
		}else if(options && !verbose) {
			pw.println("Server response: " + response.substring(response.indexOf("{")-1, response.length()-1));
			pw.close();
		}else if(!options & !verbose)
			System.out.println("Server response: " + response.substring(response.indexOf("{")-1, response.length()-1));
		else
			System.out.println("Server response: " + response);
		
		
		
	}
	
	
	private int GetArgs (String[] array) {

		int indexGet = array[array.length-1].lastIndexOf("?");
		
		
		return indexGet;
		
	}
	
	
}
