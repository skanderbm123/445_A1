import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.naming.NamingException;

import java.io.File; 
import java.io.FileNotFoundException; 
import java.util.Scanner; 

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
				
				InputStream inputStream = socket.getInputStream();
				OutputStream outputStream = socket.getOutputStream();
				
				boolean file=false;
				boolean data=false;
				
				for(int i=0 ; i < args.length ; i++) {
					if(args[i].equals("-f"))
						file = true;
					if(args[i].equals("-d"))
						data = true;
						
				}
				
				String body="";
				String InlineData="";
				String ContentFile="";
				
				if(data) {
				InlineData = CheckData(args);
				body = InlineData;
				}
				
				if(file) {
				ContentFile = CheckFile(args);
				body = ContentFile;
				}
					
	
				String[] getHeaders = CheckHeaders(args);
				
				String headers = "";
				
				for(int i=0;i<getHeaders.length;i++) {
					
					headers = headers + getHeaders[i] + "\r\n";
					
				}
				
				
				String request = "POST /post HTTP/1.0\r\n"
						+ "Content-Length: " + body.length() + "\r\n"+ "Host: "+ip.getHostName()+"\r\n"+
						headers+ "\r\n" 
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
				if(data)
				CheckVerbose(args , response , InlineData);
				if(file)
				CheckVerbose(args , response , ContentFile);
				if(!data && !file)
				CheckVerbose(args , response , "");
				
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

	private String CheckData(String[] args) {
		
		boolean data=false;
		
		for(int i=0 ; i < args.length ; i++) {
			if(args[i].equals("-d")) {
				
				if(data == true) {
				System.out.println("Duplicate inline data, you can only have one -d");
				System.exit(0);
				}
				data=true;
			  }
			}

		if(data==false) {
			return "";
		}else {
			for(int i = 0 ; i < args.length ; i++) {
				if(args[i].equals("-d"))
						return args[i+1];
	
			}			
		}

		return null;
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

	private void CheckVerbose(String[] args , StringBuilder response , String InlineData) {
		boolean verbose = false;
		for(int i=0 ; i < args.length ; i++) {
			if(args[i].equals("-v"))
				verbose = true;
		}
		
		int indexJson =response.indexOf("\"json\"") +8 ;

		if(!verbose)
			System.out.println("Server response: " + response.substring( response.indexOf("{")-1,indexJson)+ InlineData + response.substring(indexJson +4));
		else
			System.out.println("Server response: " + response.substring(0 ,indexJson)+ InlineData +response.substring(indexJson +4));
		
		
		
		/*
		if(!verbose)
			System.out.println("Server response: " + response.substring(response.indexOf("{")-1, response.length()-1));
		else
			System.out.println("Server response: " + response);*/	
	}
	
	private String CheckFile(String[] args) {
		
		boolean file=false;
		int index=0;
		for(int i=0 ; i < args.length ; i++) {
			if(args[i].equals("-f")) {
				file = true;
				index = i;
			}
				
		}

		//checks for -d and -f 
		for(int i=0 ; i < args.length ; i++) {
			if(args[i].equals("-d") && file) {
				System.out.println("Error : Either [-d] or [-f] can be used but not both");
				System.exit(0);
			}
		}
		
		if(file) {
		   
			String fileData="";
			
			 try {
			      File fileObj = new File(args[index+1]);
			      Scanner myReader = new Scanner(fileObj);
			    	fileData = myReader.nextLine();
			      myReader.close();
			    } catch (FileNotFoundException e) {
			      System.out.println("An error occurred during the lecture of the file");
			      e.printStackTrace();
			    }
		
		    return fileData;
		}
		
		return null;

	}

}


	
