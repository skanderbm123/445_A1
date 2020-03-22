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

public class UDPClient {
    public static String routerHost = "localhost";
    public static int routerPort = 3000;
    public static String serverHost = "localhost";
    public static int serverPort = 8007;

    public static final Logger logger = LoggerFactory.getLogger(UDPClient.class);
       
    public static void runClient(SocketAddress routerAddr, InetSocketAddress serverAddr , String[] args) throws IOException {
        try(DatagramChannel channel = DatagramChannel.open()){
            String msg =  createMessage(args);
            
            if(msg.getBytes().length <= Packet.MAX_PAYLOAD) {
            
            Packet p = new Packet.Builder()
                    .setType(0)
                    .setSequenceNumber(1L)
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
            logger.info("Packet: {}", resp);
            logger.info("Router: {}", router);
            String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
            logger.info("Payload: {}",  payload);
        
            keys.clear();
            }
            else {
            	// TODO: divide info into Packets
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
}