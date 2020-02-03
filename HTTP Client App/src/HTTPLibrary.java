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
			else
				System.out.println("oops");
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
