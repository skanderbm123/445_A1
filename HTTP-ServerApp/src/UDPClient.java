import java.io.IOException;
import java.util.*;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.charset.StandardCharsets.UTF_8;

public class UDPClient {
    public static String routerHost = "localhost";
    public static int routerPort = 3000;
    public static String serverHost = "localhost";
    public static int serverPort = 8007;

    public static final Logger logger = LoggerFactory.getLogger(UDPClient.class);
    public static boolean handshake = false;
    
    public static void runClient(SocketAddress routerAddr, InetSocketAddress serverAddr , String[] args) throws IOException {
        try(DatagramChannel channel = DatagramChannel.open()){
            String msg =  createMessage(args);
            long sequenceNumber = threeWayHandShake(channel,serverAddr,routerAddr);
            
            if(handshake) {
            if(msg.getBytes().length <= Packet.MAX_PAYLOAD) {
            
            Packet p = new Packet.Builder()
                    .setType(0)
                    .setSequenceNumber(sequenceNumber+1)
                    .setPortNumber(serverAddr.getPort())
                    .setPeerAddress(serverAddr.getAddress())
                    .setPayload(msg.getBytes())
                    .create();
            channel.send(p.toBuffer(), routerAddr);

            
            logger.info("Sending \"{}\" to router at {}", msg, routerAddr);

            // Try to receive a packet within timeout.
            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.register(selector, OP_READ);
            logger.info("Waiting for the response");
            selector.select(5000);

            Set<SelectionKey> keys = selector.selectedKeys();
            if(keys.isEmpty()){
                logger.error("No response after timeout");
                return;
            }

            // We just want a single response.
            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            SocketAddress router = channel.receive(buf);
            buf.flip();
            Packet resp = Packet.fromBuffer(buf);
            String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
            logger.info("Packet: {}", resp);
            logger.info("Router: {}", router);
            logger.info("Payload:");
            System.out.println(payload);
            keys.clear();
            
            //Send FIN to end connection
            }
            else {
            	// TODO: divide info into Packets
            }
        }
        }
    }

	private static String createMessage(String[] args) {
		String message=args[1]+" "+args[2]+" ";
	
		for(int i=1;i<args.length;i++) {	
			if(args[i].equals("-h")) {
				message=message+args[i]+" "+args[i+1]+" ";
			}
			if(args[i].equals("-d")) {				
				for(int j = i ; j < args.length-1; j++) {
						message=message+args[j]+" ";
				}				
			}
		}
		message=message+args[args.length-1];
		return message;
	}
	
	
	
	  private static long threeWayHandShake(DatagramChannel datagramChannel,InetSocketAddress serverAddress, SocketAddress routerAddr) throws IOException {

	        String connectionString = "Client : Asking Server for connection #SYN";
	        
	        Packet connect = new Packet.Builder()
	                .setType(1) // Ask for SYN
	                .setSequenceNumber(1L)
	                .setPortNumber(serverAddress.getPort())
	                .setPeerAddress(serverAddress.getAddress())
	                .setPayload(connectionString.getBytes())
	                .create();
	        datagramChannel.send(connect.toBuffer(), routerAddr);
	        
	        logger.info("Client : Packet SYN has been sent out");
		      
	        //timer(datagramChannel,test);


	        ByteBuffer byteBuffer = ByteBuffer.allocate(Packet.MAX_LEN);
	        byteBuffer.clear();
	        datagramChannel.receive(byteBuffer);
	        byteBuffer.flip();
	        
	        Packet packet = Packet.fromBuffer(byteBuffer);
	        long awkSeqNumber = packet.getSequenceNumber();
	        connectionString = "Client AWK connection";
	        logger.info("Message : {} " , new String(packet.getPayload(),UTF_8));
	   
	        
	        if(packet.getType()==2) {
	        	connect = new Packet.Builder()
		                .setType(3) // Send AWK and will send a request afterward
		                .setSequenceNumber(awkSeqNumber+1)
		                .setPortNumber(serverAddress.getPort())
		                .setPeerAddress(serverAddress.getAddress())
		                .setPayload(connectionString.getBytes())
		                .create();
		        datagramChannel.send(connect.toBuffer(), routerAddr);
	        }
	        
	        logger.info("Now that three-way handshake is complete, request will be sent");
	        handshake = true;
	        return connect.getSequenceNumber();
	    }
	}
	
	
	
	
	
	
	
	
	
	
