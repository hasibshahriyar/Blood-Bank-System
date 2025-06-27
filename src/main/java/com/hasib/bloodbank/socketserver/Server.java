package com.hasib.bloodbank.socketserver;

import com.hasib.bloodbank.utils.Information;
import com.hasib.bloodbank.utils.NetworkUtility;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("Server Started...");
        System.out.println(InetAddress.getLocalHost());
        HashMap<Integer, Information> clientList = new HashMap<>();
        while (true) {
            Socket socket = serverSocket.accept();
            NetworkUtility networkUtility = new NetworkUtility(socket);
            new Thread(new CreateConnection(clientList,networkUtility)).start();

        }
    }
}
