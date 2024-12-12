package com.producer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Producer implements Runnable {
    private int m_n;
    private ThreadPoolExecutor m_pool;
    private int m_port = 30000;
    private BlockingQueue<Long> m_buffer;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    class Task implements Runnable {
        private Data m_data;

        public Task(Data data) {
            this.m_data = data;
        }

        @Override
        public void run() {
            try (Socket s = new Socket("127.0.0.1", m_port);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-16LE"))) {

                // System.out.println("开始发送素数");
                bw.write(MAPPER.writeValueAsString(m_data));
                bw.flush();
                // System.out.println("已发送素数");

            } catch (UnknownHostException e) {
                System.out.println("ERROE1:" + e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                System.out.println("ERROE2:" + e.getMessage());
                m_n += m_data.data.size();
                System.out.println(m_data.data.size());
            }
        }
    }

    public Producer(int n) {
        m_n = n;
        m_pool = new ThreadPoolExecutor(4, 4, 0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(8), new ThreadPoolExecutor.DiscardPolicy());
        m_buffer = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        Random random = new Random();
        int n = m_n;
        while (m_n > 0) {
            if (m_buffer.size() < m_n)
            m_buffer.add(random.nextInt(2000000000) + (long) 2000000000);
            if (m_buffer.size() >= Math.min(10000, m_n)) {
                try {
                    // System.out.println("buffer:" + m_buffer.size());
                    if (m_pool.getTaskCount() - m_pool.getCompletedTaskCount() < 12) {
                        m_pool.execute(new Task(new Data(MAPPER.readValue(m_buffer.toString(),
                                new TypeReference<List<Long>>() {
                                }))));
                        m_n -= m_buffer.size();
                        System.out.println(m_n);
                        m_buffer.clear();
                    }
                } catch (JsonMappingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (n-m_n > 1000) {
                System.out.println(m_n);
                n = m_n;
            }
        }
        m_pool.shutdown();
        System.out.println("finish");
    }
}
