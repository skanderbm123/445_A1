import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UDPLaunch {
	
	public static String routerHost = "localhost";
	public static int routerPort = 3000;
	public static String serverHost = "localhost";
	public static int serverPort = 8007; 
	private static final Logger logger = LoggerFactory.getLogger(UDPLaunch.class);

	public static void main(String[] args) throws IOException {
		
		
		
		  if(args[0].equals("httpc") &&  verifyArgs(args)) {
			  
			  logger.info("Running client for request of type: {}", args[1]);
				logger.info("Url of the request: {}", args[args.length - 1]);
			  
	            for(int i = 0 ; i < args.length ; i++) {
	                if(args[i].equals("--router-host")) {
	                    routerHost = args[i+1];
	                }
	                else if(args[i].equals("--router-port")) {
	                    routerPort = Integer.parseInt(args[i+1]);
	                }
	                else if(args[i].equals("--server-host")) {
	                    serverHost = args[i+1];
	                }
	                else if(args[i].equals("--server-port")) {
	                    serverPort = Integer.parseInt(args[i+1]);
	                }
	                
	            }
	            
		
		  
		  
		  
		  	//run client 
		        SocketAddress routerAddress = new InetSocketAddress(routerHost, routerPort);
	            InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);
	            
	            UDPClient.runClient(routerAddress, serverAddress, args);
	 }
	}

	private static boolean verifyArgs(String[] args) {
		
		if(args[1].equalsIgnoreCase("get")) {
			
			for(int i=0;i<args.length;i++) {
				if(args[i].equals("-d")|| args[i].equals("-f")) {
					System.out.println("You cannot have a body in the GET request");
					return false;
			}
				
			}
		}
		if(args[1].equalsIgnoreCase("post")) {
			boolean file=false;
		
			for(int i=0 ; i < args.length ; i++) {
				if(args[i].equals("-f")) {
					file = true;
				
				}

			}

			//checks for -d , -f or -o
			for(int i=0 ; i < args.length ; i++) {
				if(args[i].equals("-d") && file) {
					System.out.println("Error : Either [-d] or [-f] can be used but not both");
					return false;
				}
				//to remove if TA wants -o for post
				if(args[i].equals("-o")) {
					System.out.println("Error : -o is only for GET Methods");
					return false;
				}
				//
			}
		}
		
		return true;
		
	}
	
	
}