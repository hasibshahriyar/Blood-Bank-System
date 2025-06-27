package com.hasib.bloodbank.socketserver;

import com.hasib.bloodbank.utils.Information;
import com.hasib.bloodbank.utils.NetworkUtility;

import java.util.HashMap;

public class CreateConnection implements Runnable {
    HashMap<Integer, Information> clientList;
    NetworkUtility networkUtility;
    public CreateConnection(HashMap<Integer, Information> clientList, NetworkUtility networkUtility) {
        this.clientList=clientList;
        this.networkUtility=networkUtility;
    }

    @Override
    public void run() {
        Object clintObject=networkUtility.read();
        int id=(Integer)clintObject;

        System.out.println("User : "+id+" connected");
        clientList.put(id,new Information(id,networkUtility));
        System.out.println("HashMap updated"+clientList);
        new Thread(new ReaderWriterServer(id,networkUtility,clientList)).start();
    }
}
