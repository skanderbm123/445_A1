import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;
import java.io.InputStream;
import java.io.OutputStream;

public class HTTPLibrary {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length > 0) {
			if(args[1].equals("get")) {
				HTTPLibrary.GetRequest(args);
			}
			else if (args[1].equals("post")){
				HTTPLibrary.PostRequest(args);
			}
			else {
				if(args.length == 3 && args[2].equals("post")) {
					System.out.println("Usage:\n"
							+ "\thttpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\n"
							+ "Post executes a HTTP POST request for a given URL with inline data or from file.\n"
							+ "\t-v: Prints the detail of the response such as protocol, status, and headers.\n"
							+ "\t-h: key:value Associates headers to HTTP Request with the format 'key:value'.\n"
							+ "\t-d: string Associates an inline data to the body HTTP POST request.\n"
							+ "\t-f: file Associates the content of a file to the body HTTP POST request.\n"
							+ "Either [-d] or [-f] can be used but not both.");
				}
				else if(args.length == 3 && args[2].equals("get")) {
					System.out.println("Usage:\n"
							+ "\thttpc get [-v] [-h key:value] URL\n"
							+ "Get executes a HTTP GET request for a given URL.\n"
							+ "\t-v: Prints the detail of the response such as protocol, status, and headers.\n"
							+ "\t-h: key:value Associates headers to HTTP Request with the format 'key:value'.");
				}
				else {
					System.out.println("httpc is a curl-like application but supports HTTP protocol only.\n"
							+ "Usage: \n"
							+ "\thttpc command [arguments]\n"
							+ "The commands are:\n"
							+ "\t- get: executes a HTTP GET request and prints the response.\n"
							+ "\t- post: executes a HTTP POST request and prints the response.\n"
							+ "\t- help: prints this screen.\n"
							+ "Use \"httpc help [command]\" for more information about a command");
				}
			}
		}
		else {
			System.out.println("Please enter valid arguments of the form:\n" + 
					"httpc (get|post) [-v] (-h \"k:v\")* [-d inline-data] [-f file] URL");
		}
	}
	
	// Method representing the GET operation
	public static void GetRequest(String[] args) {
		System.out.println("--GET REQUEST--");
		
		String URL = args[args.length - 1];
		
		if(!URL.equals("")) {
			try {				
				// To get IP address of URL
				InetAddress ip = InetAddress.getByName(new URL(URL).getHost());
				var socket = new Socket(ip, 80);
				
				// Setting up input and output streams
				InputStream inputStream = socket.getInputStream();
				OutputStream outputStream = socket.getOutputStream();
				
				String request = "GET /get?key1=value1 HTTP/1.0\r\n\r\n";
				
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
				boolean verbose = false;
				for(int i=0 ; i < args.length ; i++) {
					if(args[i].equals("-v"))
						verbose = true;	
				}
				
				if(!verbose)
					System.out.println("Server response: " + response.substring(response.indexOf("{")-1, response.length()-1));
				else
					System.out.println("Server response: " + response);
				
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
	
	
	// Method representing the POST operation
	public static void PostRequest(String[] args) {
		System.out.println("--POST REQUEST--");
		
		String URL = args[args.length - 1];
		
		if(!URL.equals("")) {
			try {				
				// To get IP address of URL
				InetAddress ip = InetAddress.getByName(new URL(URL).getHost());
				var socket = new Socket(ip, 80);
				
				// Setting up input and output streams
				InputStream inputStream = socket.getInputStream();
				OutputStream outputStream = socket.getOutputStream();
				
				String body = "key1=value1&key2=value2";
				
				String request = "POST /post HTTP/1.0\r\n"
						+ "Content-Type: application/x-www-form-urlencoded\r\n"
						+ "Content-Length: " + body.length() + "\r\n"
						+ "\r\n"
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
				
				System.out.println("Server response: " + response);
				
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

}
