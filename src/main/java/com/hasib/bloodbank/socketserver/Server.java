package com.hasib.bloodbank.socketserver;

import com.hasib.bloodbank.utils.Information;
import com.hasib.bloodbank.utils.NetworkUtility;
import com.hasib.bloodbank.utils.ThreadPoolManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    private static final AtomicBoolean isRunning = new AtomicBoolean(true);
    private static ServerSocket serverSocket;
    private static final ConcurrentHashMap<Integer, Information> clientList = new ConcurrentHashMap<>();
    private static final ThreadPoolManager threadPool = ThreadPoolManager.getInstance();

    public static void main(String[] args) {
        setupShutdownHook();

        try {
            serverSocket = new ServerSocket(9999);
            System.out.println("Server Started...");
            System.out.println("Server Address: " + InetAddress.getLocalHost());
            System.out.println("Listening on port: 9999");

            while (isRunning.get()) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected: " + socket.getInetAddress());

                    // Use thread pool instead of creating new threads directly
                    threadPool.executeNetworkTask(() -> {
                        try {
                            NetworkUtility networkUtility = new NetworkUtility(socket);
                            new CreateConnection(clientList, networkUtility).run();
                        } catch (Exception e) {
                            System.err.println("Error handling client connection: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });

                } catch (IOException e) {
                    if (isRunning.get()) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    private static void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Server shutdown initiated...");
            shutdown();
        }));
    }

    public static void shutdown() {
        if (isRunning.compareAndSet(true, false)) {
            System.out.println("Shutting down server...");

            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }

            // Close all client connections
            clientList.forEach((id, info) -> {
                try {
                    if (info.networkUtility != null) {
                        info.networkUtility.closeConnection();
                    }
                } catch (Exception e) {
                    System.err.println("Error closing client connection " + id + ": " + e.getMessage());
                }
            });

            clientList.clear();
            System.out.println("Server shutdown complete");
        }
    }

    public static ConcurrentHashMap<Integer, Information> getClientList() {
        return clientList;
    }

    public static boolean isRunning() {
        return isRunning.get();
    }
}
