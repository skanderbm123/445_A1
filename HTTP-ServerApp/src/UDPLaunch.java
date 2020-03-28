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
		
		logger.info("Running client for request of type: {}", args[1]);
		logger.info("Url of the request: {}", args[args.length - 1]);
		
		  if(args[0].equals("https")) {
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
	            
		  }
		  
		  
		  
		  	//run client 
		        SocketAddress routerAddress = new InetSocketAddress(routerHost, routerPort);
	            InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);
	            UDPClient.runClient(routerAddress, serverAddress, args);

	}
	
	
}