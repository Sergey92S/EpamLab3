package com.shmouradko.freecashier.runner;

import com.shmouradko.freecashier.entity.Client;
import com.shmouradko.freecashier.service.Restaurant;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Сергей on 23.10.2016.
 */
public class FreeCashierRunner {
    static Logger logger = LogManager.getLogger();
    private static final int CLIENT_COUNT = 20;
    private static final int MAX_NUMBER_OF_GOODS = 50;
    private static final int CASHIER_COUNT = 4;
    private static final int MIN_VALUE = 0;
    private static final int MAX_CLIENT_INTERVAL = 6;
    private static final int MIN_CLIENT_INTERVAL = 2;
    private static final int MIN_NUMBER_OF_GOODS = 1;

    public static void main(String[] args){
        Thread[] threads = new Thread[CLIENT_COUNT];
        Random random = new Random();
        Restaurant restaurant = Restaurant.getInstance(CASHIER_COUNT, random);

        for (int id = MIN_VALUE; id < CLIENT_COUNT; id++) {
            int numberOfGoods = random.nextInt(MAX_NUMBER_OF_GOODS) + MIN_NUMBER_OF_GOODS;
            threads[id] = new Thread(new Client(restaurant, id, numberOfGoods));
        }

        for (int i = MIN_VALUE; i < CLIENT_COUNT; i++) {
            try {
                TimeUnit.SECONDS.sleep(random.nextInt(MAX_CLIENT_INTERVAL)+MIN_CLIENT_INTERVAL);
            } catch (InterruptedException e) {
                logger.log(Level.FATAL, " This thread was interrupted while starting clients ", e);
            }
            threads[i].start();
        }

    }

}
