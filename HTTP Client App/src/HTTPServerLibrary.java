import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class HTTPServerLibrary {

    public static void main(String[] args) throws IOException {
        
        boolean flag = true;
        String userInput = "";
        Scanner sc = new Scanner(System.in);
        int port = 80;
        ServerSocket server = null;
        Socket client = null;
        boolean verbose = false;

        if(args[0].equals("httpfs")) {

            for(int i =0;i<args.length;i++){

                if(args[i].equals("-p")){
                    port = Integer.parseInt(args[i+1]) ;
                }
                if(args[i].equals("-v")){
                   verbose=true;
                }

            }
            
            while(flag){

                //userInput = sc.nextLine();
               
                if(userInput.equals("-q")) {
                    flag = false;
                    break;
                }
                    
                // Creating server and client sockets
                try {
                    server = new ServerSocket(port, 0, InetAddress.getLoopbackAddress());
                    client = server.accept();

                }
                catch(Exception e) {
                    
                    System.out.println(e.getMessage());
                }

                Scanner in = new Scanner(new InputStreamReader(client.getInputStream()));

                StringBuilder response = new StringBuilder();
                

                while(in.hasNextLine()){

                    String wtv = in.nextLine();
                    response.append(wtv+"\n");
                    
                    if(wtv.equals(""))
                        break;
                }
                
                System.out.println(response);
                client.close();
                server.close();
               
            }
            
            System.out.println("test4 : successfull");
           
        }
                
    
    }
}