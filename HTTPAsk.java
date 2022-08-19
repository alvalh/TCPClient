import java.net.*;
import java.io.*;
import tcpclient.TCPClient;

public class HTTPAsk {
    static String hostname = null;
    static int port = 0;
    static byte[] toServerBytes = new byte[0];
    static boolean shutdown = false;
    static Integer limit = null;
    static Integer timeout = null;
    static String syntax = "\r\n" + "\r\n";


    public static void main(String[] args) throws Exception {

        int serverPort = Integer.parseInt(args[0]);
        StringBuilder HTTPrequest = new StringBuilder();

        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {
                System.out.println("Listning to " + serverPort);
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection accepted");

                InputStreamReader in = new InputStreamReader(clientSocket.getInputStream());
                BufferedReader clientIn = new BufferedReader(in);

                String read = clientIn.readLine();
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                String URLstring = read;
                String[] URLarray = URLstring.split("[=&? ]");

                for (int i = 0; i < URLarray.length; i++) {
                    if (URLarray[i].equals("hostname")) {
                        hostname = URLarray[i + 1];
                    }
                    if (URLarray[i].equals("port")) {
                        port = Integer.parseInt(URLarray[i + 1]);
                    }
                    if (URLarray[i].equals("string")) {
                        toServerBytes = URLarray[i + 1].getBytes();
                    }
                    if (URLarray[i].equals("shutdown")) {
                        shutdown = true;
                    }
                    if (URLarray[i].equals("limit")) {
                        limit = Integer.parseInt(URLarray[i + 1]);
                    }
                    if (URLarray[i].equals("timeout")) {
                        timeout = Integer.parseInt(URLarray[i + 1]);
                    }
                }

                while (!(read = clientIn.readLine()).isEmpty()) {
                    HTTPrequest.append(read + "\r\n");
                }

                String response = response(URLstring, HTTPrequest.toString());
                out.writeBytes(response);

                clientSocket.close();
            }
        }
        catch (Exception e) {
            System.err.println(e);
        }
    }

    public static String response(String URLstring, String HTTPrequest) throws Exception{
        StringBuilder httpResponse = new StringBuilder();
        try{
            String[] split1 = URLstring.split(" ");
            String[] split2 = split1[1].split("\\?");

            if(!HTTPrequest.contains("Host:")){
                httpResponse.append("HTTP/1.1 400 Bad Request" + syntax);
                return httpResponse.toString();
            }

            if(hostname == null || port == 0 || split2.length > 2 || !split2[0].equals("/ask") || split1.length < 3){
                httpResponse.append("HTTP/1.1 400 Bad Request" + syntax);
                return httpResponse.toString();
            }
        }catch(Exception e){
            System.err.println(e);
        }

        try{
            TCPClient tcpClient = new TCPClient(shutdown, timeout, limit);
            byte[] serverBytes = tcpClient.askServer(hostname, port, toServerBytes);
            String serverOutput = new String(serverBytes);
            System.out.println(serverOutput);

            httpResponse.append("HTTP/1.1 200 OK" + syntax);
            httpResponse.append(serverOutput);
            String str = httpResponse.toString();
            return str;

        }catch(Exception e){
            httpResponse.append("HTTP/1.1 404 Not Found" + syntax);
            String str = httpResponse.toString();
            return str;
        }
    }
    
}