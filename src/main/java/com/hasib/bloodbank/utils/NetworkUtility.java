package com.hasib.bloodbank.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkUtility {
    Socket socket;
    ObjectInputStream readObject;
    ObjectOutputStream writeObject;

    public NetworkUtility(Socket sock) throws IOException {
        socket=sock;
        writeObject=new ObjectOutputStream(socket.getOutputStream());
        readObject=new ObjectInputStream(socket.getInputStream());
    }

    public NetworkUtility(String ip,int port) throws IOException{
        socket=new Socket(ip, port);
        writeObject=new ObjectOutputStream(socket.getOutputStream());
        readObject=new ObjectInputStream(socket.getInputStream());
    }

    public void write(Object obj){
        try {
            writeObject.writeObject(obj);
        } catch (IOException ex) {
            System.out.println("Failed to write");
            //throw ex;
        }
    }

    public Object read(){
        Object obj;
        try {
            obj = readObject.readObject();
        } catch (Exception ex) {
            System.out.println("Failed to read");
            return null;
        }
        return obj;
    }

    public Socket getSocket() {
        return socket;
    }
}
