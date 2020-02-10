import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
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
				InetAddress ip = null;

				try {
				// To get IP address of URL
				ip = InetAddress.getByName(new URL(url).getHost());
				
				}
				catch (UnknownHostException exception)
		        {
		            System.err.println("ERROR: Cannot access '" + url + "'");
		            System.exit(0);
		        }
				

				var socket = new Socket(ip, 80);
				
				// checks that body does not contains -d , -f or  -h
				CheckBody(args);
				int StatusCode = FindCode(url);
				
				// Setting up input and output streams
				InputStream inputStream = socket.getInputStream();
				OutputStream outputStream = socket.getOutputStream();
				
				
				String request = "GET "+url+" HTTP/1.0\r\nhost:"+ip.getHostName()+"\r\n\r\n";
				
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
				
				if(StatusCode>299 && StatusCode<320) {
					
					int NumberLocation = response.indexOf("Location: ")+10;
					int NumberAccess= response.indexOf("Access-Control-Allow-Origin:");
					
					String redirectUrl = response.substring(NumberLocation,NumberAccess);
		
					args[args.length-1]=redirectUrl;
	
					setUrl(redirectUrl);
					System.out.println("Server response: " + response);
					
					System.out.println(redirectUrl);
					
					operation(args);
				}
				else {
				// Check whether 'verbose' option was passed in command arguments
				CheckOptions(args , response, StatusCode);
				}
				
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
	private void CheckOptions(String[] args , StringBuilder response, int code) {
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
		
		if(code>299) {
			if(!options)
			System.out.println("Server response: " + response);
			else {
				pw.println("Server response: " + response);
				pw.close();
			}
		}else {
		
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
		
		
	}

	
	private void CheckBody(String[] args) {
		for(int i=0;i<args.length;i++) {
			if(args[i].equals("-h") || args[i].equals("-d")|| args[i].equals("-f")) {
				System.out.println("You cannot have a body in the GET request");
				System.exit(0);
			}
			
		}

	}
	
	private int FindCode(String url) {
		int code=0;
		try {
		     URL website = new URL(url);
		     HttpURLConnection.setFollowRedirects(false);
		     HttpURLConnection connection = (HttpURLConnection) website.openConnection();
		     connection.connect();
		     code = connection.getResponseCode();

		     connection.disconnect();
		}
		catch (IOException e) {
		  e.printStackTrace();

		}
		
		
		
		return code;
	}
	
	
}
