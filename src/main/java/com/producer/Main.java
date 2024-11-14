package com.producer;

public class Main {
    public static void main(String[] args) {
        new Thread(new Producer(100000)).start();
    }
}