import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
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

		String fullUrl = url;
		String fileName = "";
		String port="80";

		if(url.contains("localhost"))
			fileName = (url.substring(url.indexOf("/", 16)));

		if(url.contains("localhost"))
			port=url.substring(url.indexOf(":",14)+1,url.indexOf("/",16));

		if(!url.equals("")) {
			try {				
				InetAddress ip = null;
			
				try {
					// To get IP address of URL
					if(url.contains("localhost"))
						setUrl("http://localhost/");
					
					ip = InetAddress.getByName(new URL(url).getHost());
				}
				catch (UnknownHostException exception)
		        {
					
		            System.err.println("ERROR: Cannot access '" + url + "'");
		            System.exit(0);
		        }
			

				
				// checks that body does not contains -d , -f or  -h
				CheckBody(args);
				
				String[] getHeaders = CheckHeaders(args);
				
				String headers = "";
				
				for(int i=0;i<getHeaders.length;i++) {
					
					headers = headers + getHeaders[i] + "\r\n";
					
				}

				Socket socket = new Socket(ip, Integer.parseInt(port));
				String request = "GET " + url + " HTTP/1.0\r\n" + headers + "\r\nhost:" + ip.getHostName() + "\r\n\r\n";
				
				// Setting up input and output streams
				InputStream inputStream = socket.getInputStream();
				OutputStream outputStream = socket.getOutputStream();


				if(url.contains("localhost")){ 

					setUrl(fileName);
						request = "GET " + url + " HTTP/1.0\r\n" + headers;
					
						outputStream.write(request.getBytes());
						outputStream.flush();
						System.out.println(request);

				}else{
				
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
					
					//Check status code
					int StatusCode = FindCode(response);
					
					if(StatusCode>299 && StatusCode<320) {

						boolean options = false;
						String outputTxtFile = "";
						int start;
						int end;
					
						if(url.substring(url.length()-1).equalsIgnoreCase("1")) {
							start = response.indexOf("URL: ")+url.length();
							end= response.indexOf("</a>.");
							
							String redirectUrl = response.substring(start,end);
							
							System.out.println(redirectUrl);
							System.out.println();
							
							args[args.length-1]=redirectUrl;
							setUrl(redirectUrl);
							
						}else {
						
						start = response.indexOf("URL: ")+16+url.length();
						end= response.indexOf("</a>.");
						
						String redirectUrl = response.substring(start,end);
						System.out.println(redirectUrl);
						System.out.println();
						
						
						args[args.length-1]=redirectUrl;
						setUrl(redirectUrl);
						
						System.out.println("Server response: " + response);
						System.out.println();
						
						for(int i=0 ; i < args.length ; i++) {
							
							if(args[i].equals("-o")) {
								options = true;
							}
							
							if(options == true) {
								outputTxtFile = args[i+1];
								break;
							}			
						}	
						
						if(options) {
							try {
								File file = new File(outputTxtFile);
								FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
								PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outputTxtFile, true)));
								pw.println();
								pw.println("Server response: " + response);
								pw.close();
								
							}
							catch(FileNotFoundException e) {
								System.out.println("File " + outputTxtFile + " not found.");
							}
							catch(IOException e) {
								System.out.println("File not created");
							}
							
							}

						}
						
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
			 FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			 pw = new PrintWriter(new BufferedWriter(new FileWriter(outputTxtFile, true)));
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

		if(count==0) {
			return new String[0];
		}
		
			String[] heads = new String[count];
			
			// for headers
		for(int i = 0 ; i < args.length ; i++) {
			heads[i] = args[index+1];
		
			if(args[index+2].equals("-d") || args[index+2].equals("-f") || args[index+2].contains("http"))
				break;

			index=index+2;
			
		}

		// if dupes found in headers array
		for(int i=0;i<heads.length;i++) {
			for(int j=(i+1);j<heads.length;j++) {
				if(heads[j].equalsIgnoreCase(heads[i])) {
					System.out.println("Duplicate found in headers, please make sure to have no duplicate ");
					System.exit(0);
				}
			}
		}
		return heads; 

	}
	
	// Method that will find the HTTP response Status Code
	private int FindCode(StringBuilder response) {
		String statusCode = response.substring(response.indexOf("1.1") + 4, response.indexOf("1.1") + 7);
		int sc = Integer.parseInt(statusCode);
		return sc;
	}
	
	private void CheckBody(String[] args) {
		for(int i=0;i<args.length;i++) {
			if( args[i].equals("-d")|| args[i].equals("-f")) {
				System.out.println("You cannot have a body in the GET request");
				System.exit(0);
			}
			
		}

	}	
	
}
