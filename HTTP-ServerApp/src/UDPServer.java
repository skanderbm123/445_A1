import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

public class UDPServer {

    private static final Logger logger = LoggerFactory.getLogger(UDPServer.class);
    private static File file;
    private static File folder = new File("./.");
    private static int numberOfCharacters = 0;
    private static String payload = "";
    private static String pathString = "";
    private static int count=0;
    
    public static void main(String[] args) throws IOException {
       OptionParser parser = new OptionParser();
       
       parser.acceptsAll(asList("port", "p"), "Listening port")
                .withOptionalArg()
                .defaultsTo("8007");

        OptionSet opts = parser.parse(args);
        int port = Integer.parseInt((String) opts.valueOf("port"));
        UDPServer server = new UDPServer();
        server.listenAndServe(port);

    }

	private void listenAndServe(int port) throws IOException {

		try (DatagramChannel channel = DatagramChannel.open()) {
			channel.bind(new InetSocketAddress(port));
			logger.info("EchoServer is listening at {}", channel.getLocalAddress());
			ByteBuffer buf = ByteBuffer
                .allocate(Packet.MAX_LEN)
                .order(ByteOrder.BIG_ENDIAN);
			
			for (; ; ) {
				buf.clear();
				SocketAddress router =  new InetSocketAddress("localhost",3000);
				channel.receive(buf);
			
				
				// Parse a packet from the received raw data.
				buf.flip();
				Packet packet = Packet.fromBuffer(buf);
				buf.flip();
				
				if(packet.getType()==Packet.DATA) {
				payload= payload+new String(packet.getPayload(), UTF_8);
				String[] args = payload.split("\\s+");
				
				logger.info("Method used: {}", args[0]);
				
				if(args[0].contains("get")) {
					// Perform GET operation
					getRequest(args);
				}
				else if(args[0].contains("post")) {
					// Perform POST operation
					postRequest(args);
				}
				else {
					payload="Must ask for a GET or POST request";
				}
	            
	            logger.info("Packet: {}", packet);
	            logger.info("Router: {}", router);
	            logger.info("Payload:");
	            System.out.println(payload);
	
	            // Send the response to the router not the client.
	            // The peer address of the packet is the address of the client already.
	            // We can use toBuilder to copy properties of the current packet.
	            // This demonstrate how to create a new packet from an existing packet.
	            Packet resp = packet.toBuilder()
	                    .setPayload(payload.getBytes())
	                    .create();
	            channel.send(resp.toBuffer(), router);
	            
	            payload="";
	            
				}else if(packet.getType()==Packet.DATAPART) {
					
					if(count==0) {
						payload= new String(packet.getPayload(), UTF_8);
					}
					
					if(payload.contains("Message separated in")) {
						count=Integer.parseInt(payload.substring(payload.length()-1));
						payload="";

					}
					else {
					
						String[] messages = new String[count];
						
					for(int i =0;i<count-1;i++) {
							messages[i]="";
							messages[i]=new String(packet.getPayload(), UTF_8);
						 	/*Packet resp = packet.toBuilder()
				                    .setPayload(("Message received").getBytes())
				                    .create();
				            channel.send(resp.toBuffer(), router);
				            */
				            
				            //retrieve next packet
					
				            buf.clear();
							router =  new InetSocketAddress("localhost",3000);
							channel.receive(buf);
						
							// Parse a packet from the received raw data.
							buf.flip();
						    packet = Packet.fromBuffer(buf);
							buf.flip();
							
          
					}
			
					
					messages[count-1]=new String(packet.getPayload(), UTF_8);
					
					missingPacket(messages,packet,channel,router,buf);	
					
					payload = String.join("", messages);
				
					String[] args = payload.split("\\s+");
					
					logger.info("Method used: {}", args[0]);
					
					System.out.println();
					
					if(args[0].contains("get")) {
						// Perform GET operation
						getRequest(args);
					}
					else if(args[0].contains("post")) {
						// Perform POST operation
						postRequest(args);
					}
					
					    logger.info("Packet: {}", packet);
			            logger.info("Router: {}", router);
			            logger.info("Payload:");
			            System.out.println(payload);
			
			            //retrieve next packet
			            buf.clear();
						router =  new InetSocketAddress("localhost",3000);
						channel.receive(buf);
					
						
						// Parse a packet from the received raw data.
						buf.flip();
					    packet = Packet.fromBuffer(buf);
						buf.flip();
						
						
			
			            
			            // Send the response to the router not the client.
			            // The peer address of the packet is the address of the client already.
			            // We can use toBuilder to copy properties of the current packet.
			            // This demonstrate how to create a new packet from an existing packet.
						String verify = new String(packet.getPayload(), UTF_8);
						if(verify.contains("Request sent, waiting for response")) {
					
			            Packet resp = packet.toBuilder()
			                    .setPayload(payload.getBytes())
			                    .create();
			            channel.send(resp.toBuffer(), router);
						
			            payload="";
			        	file=null;
			        	count=0;
			        	messages=null;
						}
					}
				}
				 else if(packet.getType()==Packet.SYN) {
					threeWayHandShake(packet,channel,router,buf);
				}else if(packet.getType()==Packet.ACK) {
					threeWayHandShake(packet,channel,router,buf);
				}
	        }
	    }
	}
	
	private void missingPacket(String[] messages, Packet packet, DatagramChannel channel, SocketAddress router, ByteBuffer buf) throws IOException {
		boolean allFilled=true;
		
		for(int i=0;i<messages.length;i++) {
			if(messages[i].equalsIgnoreCase("")) {
				Packet resp = packet.toBuilder()
	                    .setPayload(("Missing packet "+i).getBytes())
	                    .create();
	            channel.send(resp.toBuffer(), router);
	            
	            buf.clear();
				router =  new InetSocketAddress("localhost",3000);
				channel.receive(buf);
			
				// Parse a packet from the received raw data.
				buf.flip();
			    packet = Packet.fromBuffer(buf);
				buf.flip();
				
				messages[i]=new String(packet.getPayload(), UTF_8);
	            
	            allFilled=false;
			}
		}
		
		if(!allFilled)
		missingPacket(messages,packet,channel,router,buf);
		
		
		return;
	}

	private void threeWayHandShake(Packet packet, DatagramChannel channel, SocketAddress router, ByteBuffer buf) throws IOException {
		if(packet.getType()==Packet.SYN) {
		  	logger.info("Server : Packet SYN received");
	     
	        String connection = "Server : synchronization acknowledged";
	        logger.info(connection);
	        Packet response = packet.toBuilder().setSequenceNumber(packet.getSequenceNumber() + 1).setType(2).setPayload(connection.getBytes()).create();
	        channel.send(response.toBuffer(), router);
	    	logger.info("Server : Packet SYN-ACK packet has been sent out");
			}else if(packet.getType()==3) {
			logger.info("Message : {}",new String(packet.getPayload(),UTF_8));
			}
	}

	// UDP Server POST operation
	private static void postRequest(String[] args) throws IOException {
		String result = "";
		String headers = "";
		String url = args[args.length-1];  
        String fileName = getName(url);
        fileName = fileName.trim();
        String HTTPVersion = "";
        file = null;
        String data = "";
         
        // Request : UDPServer get HTTP/1.0 "http://localhost:80/eric.txt"
        for(int i = 0; i < args.length; i++) {
        	if(args[i].contains("HTTP")) {
        		HTTPVersion = args[i];
        	}
        	else if (args[i].equals("-d")) {
        		for(int j = i+1 ; j < args.length; j++) {
        			if(!(args[j].contains("http")))
        			data = data+args[j]+" ";
        			else
        				break;
				}
        		
        	}
        	else if (args[i].equals("-h")) {
        		headers = headers + args[i+1]+"\n";
        	}
        }
        
        if(!(pathString.equals(""))){ 
            File f = new File(pathString);
            f.mkdirs();
        }
       
        
        try{ 
            if(!(pathString.equals("")))
                file = new File(pathString+"/"+fileName);
            else
                file = new File(fileName);
         
            if(file.createNewFile() || file.canWrite()){ 
                result = HTTPVersion + " 200 OK\n";

                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
                pw.println(data);
                pw.close();
            }
            else{
                result = HTTPVersion + " 403 Forbidden\n";       
            }
            
            // New server's response
            if(!(result == "" && headers == "" && data == ""))
            	payload = result + headers + "Content-Length: "+(file.length()-1)+"\n"+data;
            else 
            	payload = "Must ask for a GET or POST request";
        }
        catch(FileNotFoundException e){
            System.out.println("An error occured while creating the file " + fileName);
            e.printStackTrace();
        }
      
	}

	// UDP Server GET operation
	private static void getRequest(String[] args) {	
		
		String result = "";
		String headers = "";
	    String HTTPVersion = "";
	    String data = "";
		String fileName = getName(args[args.length-1]);
        fileName = fileName.trim();
		
		 for(int i = 0; i < args.length; i++) {
	        	if(args[i].contains("HTTP")) {
	        		HTTPVersion = args[i];
	        	}else if (args[i].equals("-h")) {
	        		headers = headers + args[i+1]+"\n";
	        	}
	        }
		
		if(fileName.length()>0) {
			getAllFiles(folder,fileName);

			if(!(file == null)) {
				// return all files in directory
			      result = "GET "+HTTPVersion+" 200 OK \r\n"+headers+readFile(file); 
			      payload = result;
				
				
			}else {
				// return content of fileName
				payload = "GET "+HTTPVersion+" 404 Not Found \r\n"+headers;
			}
			
			
	
		}else {
			  result = "GET "+HTTPVersion+" 200 OK \r\n"+headers+getAllPath(folder,fileName);; 
		      payload = result;
		}
		file=null;
	}	
	
	private static String getAllPath(File folder, String fileName) {
		
		  File[] filesList = folder.listFiles();
		  String response="";
	        fileName = fileName.trim();
          for(File f : filesList){
              if(f.isDirectory()) {
            	
            	  response = response+f.getName()+"\n";
                  getAllFiles(f,fileName);
              }if(f.isFile()){
            	  response = response+f.getName()+"\n";
            	
              }
          	}
          return response;
          }
      
	

	private static void getAllFiles(File folder , String name) {
		
			File[] filesList = folder.listFiles();
			name = name.trim();
			
		    for(File f : filesList){
		        if(f.isDirectory())
		            getAllFiles(f,name);
		      
		        if(f.isFile()){
		            if(name.equalsIgnoreCase(f.getName())) {
		            	
		            	file = new File(f.getPath());
		            }
		        }
		}
		
	}
	
	private static String readFile(File f){
			String fileData="";
			
			try {
	            Scanner myReader = new Scanner(f);
	            while(myReader.hasNextLine()){ 
	                fileData = fileData + myReader.nextLine();
	                fileData = fileData +"\n";
	            }
	            myReader.close();
	              
			    } catch (FileNotFoundException e) {
			      System.out.println("An error occurred during the lecture of the file");
			      e.printStackTrace();
	            }
	      numberOfCharacters=fileData.length()-1;
	            
		return fileData;
		}
	
	private static String getName(String url) {
		
		String fileName = "";
	
		if(url.contains("localhost"))
			fileName = (url.substring(url.indexOf("/", 16)+1));
		
		return fileName;
	}
	
}