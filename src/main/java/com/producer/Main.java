package com.producer;

public class Main {
    public static void main(String[] args) {
        new Thread(new Producer(10000)).start();
    }
}