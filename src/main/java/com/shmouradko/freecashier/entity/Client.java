package com.shmouradko.freecashier.entity;

import com.shmouradko.freecashier.service.Restaurant;

/**
 * Created by Сергей on 23.10.2016.
 */
public class Client implements Runnable {
	private int number;
	private int queue;
    private int numberOfGoods;
    private int place;
    private Restaurant restaurant;

    public Client(Restaurant restaurant, int number, int numberOfGoods){
        this.restaurant = restaurant;
        this.number = number;
        this.numberOfGoods = numberOfGoods;
    }

    public void run() {
        restaurant.addToCashier(this);
        commentAdToCashier();
        if(restaurant.reachBestQueue(this)) {
            commentReachBestQueue();
        }
        if(restaurant.changePlace(this)) {
            commentChangePlace();
        }
        restaurant.startServe(this);
        restaurant.finishServe(this);

    }

    public int getNumber() {
        return number;
    }

    public int getQueue() {
        return queue;
    }

    public void setQueue(int queue) {
        this.queue = queue;
    }

    public int getNumberOfGoods() {
        return numberOfGoods;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    private void commentAdToCashier(){
        System.out.println("Client at number "+number+" with number of goods "+numberOfGoods+" add to cashier number "+queue+" and get place number "+place);
    }

    private void commentReachBestQueue(){
        System.out.println("Client at number "+number+" go to cashier number "+ queue);
    }

    public void commentChangePlace(){
        System.out.println("Client at number "+number+" change place with another client and now he has number of goods "+numberOfGoods+" add to cashier number "+queue+" and get place number "+place);
    }

}
