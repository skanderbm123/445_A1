import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

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
	
				String request = "GET /get?"+args[args.length-1].substring(KeysValue+1)+" HTTP/1.0\r\n\r\n";
				
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
	private int GetArgs (String[] array) {

		int indexGet = array[array.length-1].lastIndexOf("?");
		
		
		return indexGet;
		
	}
	
	
}
