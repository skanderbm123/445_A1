import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

public class HTTPServerLibrary {
    private static File file;
    private static int numberOfCharacters = 0;

    public static void main(String[] args) throws IOException {
        
        boolean flag = true;
        String userInput = "";
        Scanner sc = new Scanner(System.in);
        int port = 80;
        ServerSocket server = null;
        Socket client = null;
        boolean verbose = false;
        String path="";

        if((args.length)>1 && args[1].equals("help")){
            System.out.println("-v Prints debugging messages.");
            System.out.println("-p Specifies the port number that the server will listen and serve at. Default is 8080. ");
            System.out.println("-d   Specifies the directory that the server will use to read/write requested files. Default is the current directory when launching the application. ");
            System.out.println();
          
        }else if(args[0].equals("httpfs")) {

            for(int i =0;i<args.length;i++){

                if(args[i].equals("-p")){
                    port = Integer.parseInt(args[i+1]);
                    System.out.println("New port is "+port);
                }
                if(args[i].equals("-v")){
                   verbose=true;
                }
                if(args[i].equals("-d")){
                    path = args[i+1];
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
                    
                    if(verbose) {
                        System.out.println("Initializing Server and Client sockets...");
                    }
                }
                catch(Exception e) {
                    
                    System.out.println(e.getMessage());
                }

                Scanner in = new Scanner(new InputStreamReader(client.getInputStream()));
                
                if(verbose) {
                    System.out.println("Reading data...");
                }

                StringBuilder response = new StringBuilder();
                String fileName = "";
                String HttpVersion="";
                String method="";
                int counter = 0;

                while(in.hasNextLine()){

                    String wtv = in.nextLine();
                    response.append(wtv+"\n");
                    
                    if(counter == 0) {
                        method = wtv.substring(0,4);
                        fileName = wtv.substring(5, wtv.indexOf("HTTP")-1);
                        HttpVersion = wtv.substring(wtv.indexOf("HTTP"));
                        counter++;
                    }

                    if(wtv.equals(""))
                        break;
                }

                if(!method.contains("GET")){ 
                    
                    postOperation(response.toString(),HttpVersion,verbose,path);
                 }else {
                    if(fileName.length()==0) {
                        System.out.print(response+"\n");
                        File folder = new File("../.");
                        getAllFiles(folder,true," ");
                    
                    }
                    else if(fileName.length()>1) {
                        File folder = new File("../../.");
                        getAllFiles(folder,false,fileName);	
                        
                        if(file==null){			
                                System.out.println(HttpVersion + " 404 Not Found");
                                System.out.println(response);
                        }else if(!(file.canRead())){ 
                            System.out.println(HttpVersion + " 403 Forbidden");
                            System.out.println(response);
                            file=null;
                        }else{ 
                            System.out.println(HttpVersion + " 200 OK");
                        }
                   }

                
                    if(file!=null){ 
                        System.out.print(response);
                        String outputFile = readFile(file);
                        System.out.println("Content-Length: "+numberOfCharacters+"\n");
                        System.out.println(outputFile);
                    }
                    
                    numberOfCharacters=0;
                    System.out.println();
                    System.out.println();
                    file = null;
                }
                if(verbose){
                    System.out.println("Server Socket and Client Socket terminated !");
                }
                client.close();
                server.close();
            }
        }
    }

    private static void postOperation(String response, String HttpVersion, boolean verbose, String pathString) throws IOException {
      
        String fileName = response.substring(6, response.indexOf("HTTP")-1);
        File file=null;
        //String data = response.substring(response.indexOf("\n",response.indexOf("Host:")),response.length()-1);
        String data = response;
        
        if(!(pathString.equals(""))){ 
            File f = new File(pathString);
            f.mkdirs();
        }
        
        try{ 
            if(!(pathString.equals("")))
                file = new File(pathString+"/"+fileName);
            else
                file = new File(fileName);
            if(verbose){
                System.out.println("Writing into file for POST method...");
            }
            if(file.createNewFile() || file.canWrite()){ 
                System.out.println(HttpVersion + " 200 OK");
                System.out.print("POST response:\n" + response);

                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
                pw.println(data);
                pw.close();
                if(verbose){
                    System.out.println("Successfuly wrote into file!");
                }
            }else{
                System.out.println(HttpVersion + " 403 Forbidden");
                System.out.print("POST response:\n" + response);
                if(verbose){
                    System.out.println("Forbidden to write into file");
                }
        }
        }catch(FileNotFoundException e){
            System.out.println("An error occured while creating the file " + fileName);
            e.printStackTrace();
        }
    }

    private static void getAllFiles(File folder, boolean OnlyLook , String name) {

		if(OnlyLook) {
           
            File[] filesList = folder.listFiles();
            for(File f : filesList){
                if(f.isDirectory())
                    getAllFiles(f,OnlyLook,name);
                if(f.isFile()){
                   
                    System.out.println(f.getName());
                }
            }
        }
		
		if(!OnlyLook) {
			File[] filesList = folder.listFiles();
		    for(File f : filesList){
		        if(f.isDirectory())
		            getAllFiles(f,OnlyLook,name);
		      
		        if(f.isFile()){
		            if(name.equalsIgnoreCase(f.getName())) {
		            	file = new File(f.getPath());
		            }
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
}