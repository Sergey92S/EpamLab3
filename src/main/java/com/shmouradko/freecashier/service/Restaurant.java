package com.shmouradko.freecashier.service;

import com.shmouradko.freecashier.entity.Cashier;
import com.shmouradko.freecashier.entity.Client;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Сергей on 23.10.2016.
 */
public class Restaurant {
    static Logger logger = LogManager.getLogger();
    private static final int MIN_VALUE = 0;
    private static final int GOODS_INTERVAL = 5;
    private static final int FIRST_PLACE = 1;
    private static final int INCREMENT = 1;
    private static final int DECREMENT = 1;
    private static Restaurant restaurantInstance;
    private static Lock lock = new ReentrantLock();
    private static Condition condition = lock.newCondition();
    private static AtomicBoolean isCreated = new AtomicBoolean(false);
    private final int cashierCounts;
    private List<Cashier> cashierArray;
    private List<Client> clientList;
    private Random random;

    private Restaurant(int cashierCounts, Random random){
        this.cashierCounts = cashierCounts;
        this.random = random;
        cashierArray = new ArrayList<Cashier>();
        clientList = new ArrayList<Client>();
        for(int i = MIN_VALUE; i < cashierCounts; i++){
            cashierArray.add(new Cashier());
        }
        cashierArray = Collections.unmodifiableList(cashierArray);
    }

    public static Restaurant getInstance(int cashierCounts, Random random){
        if(!isCreated.get()){
            lock.lock();
            try{
                if(!isCreated.get()){
                    restaurantInstance = new Restaurant(cashierCounts, random);
                    isCreated.set(true);
                }
            }finally {
                lock.unlock();
            }
        }
        return restaurantInstance;
    }

    public void addToCashier(Client client){
        lock.lock();
        try {
            int numberOfCashier = random.nextInt(cashierCounts);
            clientList.add(client);
            cashierArray.get(numberOfCashier).setQueueSize(cashierArray.get(numberOfCashier).getQueueSize()+INCREMENT);
            client.setPlace(cashierArray.get(numberOfCashier).getQueueSize());
            client.setQueue(numberOfCashier);
        } finally {
            lock.unlock();
        }
    }

    public boolean reachBestQueue(Client client){
        lock.lock();
        try {
            int bestQueue = client.getQueue();
            for(int i = MIN_VALUE; i < cashierArray.size(); i++){
                if(cashierArray.get(i).getQueueSize()+INCREMENT < cashierArray.get(bestQueue).getQueueSize()){
                    bestQueue = i;
                }
            }
            if(bestQueue != client.getQueue()){
                cashierArray.get(client.getQueue()).setQueueSize(cashierArray.get(client.getQueue()).getQueueSize()-DECREMENT);
                cashierArray.get(bestQueue).setQueueSize(cashierArray.get(bestQueue).getQueueSize()+INCREMENT);
                client.setQueue(bestQueue);
                client.setPlace(cashierArray.get(bestQueue).getQueueSize());
                return true;
            }
        }finally {
            lock.unlock();
        }
        return false;
    }

    public boolean changePlace(Client client){
        lock.lock();
        try{
            for(Client bufferClient: clientList){
                if((client.getNumberOfGoods()+GOODS_INTERVAL) < bufferClient.getNumberOfGoods() && client.getPlace() > bufferClient.getPlace() && bufferClient.getPlace() != FIRST_PLACE){
                    int bufferQueue = bufferClient.getQueue();
                    int bufferPlace = bufferClient.getPlace();
                    bufferClient.setQueue(client.getQueue());
                    bufferClient.setPlace(client.getPlace());
                    bufferClient.commentChangePlace();
                    client.setQueue(bufferQueue);
                    client.setPlace(bufferPlace);
                    return true;
                }
            }
        }finally {
            lock.unlock();
        }
        return false;
    }

    public void startServe(Client client){
        while(true) {
            lock.lock();
                try {
                    condition.await(client.getNumberOfGoods(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    logger.log(Level.FATAL, " This thread was interrupted while starting the serve ", e);
                }finally {
                    lock.unlock();
                }
            if (client.getPlace() == FIRST_PLACE) {
                cashierArray.get(client.getQueue()).serve(client);
                break;
            }
        }
    }

    public void finishServe(Client client){
        lock.lock();
        try {
            cashierArray.get(client.getQueue()).setQueueSize(cashierArray.get(client.getQueue()).getQueueSize()-DECREMENT);
            clientList.remove(client);
            for(Client bufferClient: clientList){
                if(bufferClient.getQueue() == client.getQueue()){
                    bufferClient.setPlace(bufferClient.getPlace()-DECREMENT);
                }
            }
        }finally {
            lock.unlock();
        }
    }

}
