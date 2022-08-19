package tcpclient;

import java.net.*;
import java.io.*;

public class TCPClient {

    public boolean shutdown;
    public Integer timeout;
    public Integer limit;

    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
        this.shutdown = shutdown;
        this.timeout = timeout;
        this.limit = limit;
    }

    public byte[] askServer(String hostname, int port, byte[] toServerBytes) throws IOException {
        Integer length = 0;

        if(limit == null)   {limit = Integer.MAX_VALUE;}
        if(timeout == null) {timeout = Integer.MAX_VALUE;}

        try {
            Socket clientSocket = new Socket(hostname, port);
            clientSocket.setSoTimeout(timeout);
            clientSocket.getOutputStream().write(toServerBytes);

            if(shutdown == true) {
                clientSocket.shutdownOutput();
                System.out.println("*Client closed connection due to shutdown*");
                System.out.println();
            }  

            byte[] fromServerBuffer = new byte[1024];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            try{
                int bufferSize;
                while ((bufferSize = clientSocket.getInputStream().read(fromServerBuffer)) != -1) {         
                    
                        if(length + bufferSize > limit){
                            outputStream.write(fromServerBuffer, 0, limit - length);
                            System.out.println("*Client closed connection due to limit*");
                            System.out.println();
                            break;
                        }

                        outputStream.write(fromServerBuffer, 0, bufferSize);
                        length = length + bufferSize;
               }
            }
            catch(IOException e){
                clientSocket.close();
                return outputStream.toByteArray();
            }

            clientSocket.close();
            return outputStream.toByteArray();
        }

        catch (IOException e) {
            System.out.println(e);
            throw new IOException(e);
        }

    }
}
