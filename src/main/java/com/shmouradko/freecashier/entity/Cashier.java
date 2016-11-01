package com.shmouradko.freecashier.entity;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Сергей on 23.10.2016.
 */
public class Cashier {
    static Logger logger = LogManager.getLogger();
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private int queueSize;

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    private void commentStartServe(Client client){
        System.out.println("start serve for client "+client.getNumber());
    }

    private void commentFinishServe(Client client){
        System.out.println("finish serve for client "+client.getNumber());
    }

    public void serve(Client client){
        lock.lock();
        commentStartServe(client);
        try {
            condition.await(client.getNumberOfGoods(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.FATAL, " This thread was interrupted while serving ", e);
        }finally {
            commentFinishServe(client);
            lock.unlock();
        }
    }

}
