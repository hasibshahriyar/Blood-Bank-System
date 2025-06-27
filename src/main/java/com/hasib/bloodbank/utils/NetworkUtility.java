package com.hasib.bloodbank.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NetworkUtility {
    private Socket socket;
    private ObjectOutputStream writeObject;
    private ObjectInputStream readObject;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public NetworkUtility(Socket sock) throws IOException {
        this.socket = sock;
        try {
            writeObject = new ObjectOutputStream(socket.getOutputStream());
            readObject = new ObjectInputStream(socket.getInputStream());
            isConnected.set(true);
        } catch (IOException e) {
            closeConnection();
            throw e;
        }
    }

    public NetworkUtility(String ip, int port) throws IOException {
        try {
            socket = new Socket(ip, port);
            writeObject = new ObjectOutputStream(socket.getOutputStream());
            readObject = new ObjectInputStream(socket.getInputStream());
            isConnected.set(true);
        } catch (IOException e) {
            closeConnection();
            throw e;
        }
    }

    public synchronized void write(Object obj) {
        if (!isConnected.get()) {
            System.err.println("Cannot write - connection is closed");
            return;
        }

        lock.writeLock().lock();
        try {
            writeObject.writeObject(obj);
            writeObject.flush();
        } catch (IOException e) {
            System.err.println("Error writing object: " + e.getMessage());
            closeConnection();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public synchronized Object read() {
        if (!isConnected.get()) {
            System.err.println("Cannot read - connection is closed");
            return null;
        }

        lock.readLock().lock();
        try {
            return readObject.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error reading object: " + e.getMessage());
            closeConnection();
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean isConnected() {
        return isConnected.get() && socket != null && !socket.isClosed() && socket.isConnected();
    }

    public synchronized void closeConnection() {
        if (isConnected.compareAndSet(true, false)) {
            lock.writeLock().lock();
            try {
                if (writeObject != null) {
                    try {
                        writeObject.close();
                    } catch (IOException e) {
                        System.err.println("Error closing output stream: " + e.getMessage());
                    }
                }

                if (readObject != null) {
                    try {
                        readObject.close();
                    } catch (IOException e) {
                        System.err.println("Error closing input stream: " + e.getMessage());
                    }
                }

                if (socket != null && !socket.isClosed()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        System.err.println("Error closing socket: " + e.getMessage());
                    }
                }

                System.out.println("Network connection closed successfully");
            } finally {
                lock.writeLock().unlock();
            }
        }
    }
}
