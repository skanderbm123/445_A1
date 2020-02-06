import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

public class HTTPPost {

	String url;
	
	public HTTPPost() {
		System.out.println("--POST REQUEST--");
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
				var socket = new Socket(ip, 80);
				
				
				// Setting up input and output streams
				InputStream inputStream = socket.getInputStream();
				OutputStream outputStream = socket.getOutputStream();
				
				String body = "key1=value1&key2=value2";
				
				String[] headers = CheckHeaders(args);
				
				String contextType = "";
				
				for(int i=0;i<headers.length;i++) {
					
					contextType = headers[i] + "\r\n";
					
				}
				
				
				String request = "POST /post HTTP/1.0\r\n"
						+ "Content-Length: " + body.length() + "\r\n"+
						contextType+ "\r\n"
						+ body;
				
				outputStream.write(request.getBytes());
				outputStream.flush();
				
				StringBuilder response = new StringBuilder();
				
				int responseData = inputStream.read();
				
				// Convert response bytes to String
				while(responseData != -1) {
					response.append((char) responseData);
					responseData = inputStream.read();
				}
				
				
			
				
				// Check whether 'verbose' option was passed in command arguments
				CheckVerbose(args , response);
				
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

	private String[] CheckHeaders(String[] args) {
		
		
		int index = 0;
		int count=0;
		//find index of headers
		for(int i=0 ; i < args.length ; i++) {
			
			if(args[i].equals("-h")) {
				count++;		
				if(count==1)
					index=i;
			}
			
		
			
		}
		

			String[] heads = new String[count];
			
			// for headers
		for(int i = 0 ; i < args.length ; i++) {
			
			
			heads[i] = args[index+1];

		
			if(args[index+2].equals("-d") || args[index+2].equals("-f") || args[index+2].contains("http"))
				break;

			index=index+2;
			
		}
		
		
		return heads; 
		
		
	}

	private void CheckVerbose(String[] args , StringBuilder response) {
		boolean verbose = false;
		for(int i=0 ; i < args.length ; i++) {
			if(args[i].equals("-v"))
				verbose = true;
		}
		
		if(!verbose)
			System.out.println("Server response: " + response.substring(response.indexOf("{")-1, response.length()-1));
		else
			System.out.println("Server response: " + response);
		
	}
	
	
}
