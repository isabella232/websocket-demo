package com.xoom.research;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProducerImpl implements Producer {
    private final Set<Consumer> consumers = new HashSet<Consumer>();
    private final ScheduledExecutorService scheduledExecutorService;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

    public ProducerImpl() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(new Worker(), 2, 5, TimeUnit.SECONDS);
        while (true) {
            try {
                produce(queue.take());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void produce(Object o) {
        for (Consumer consumer : consumers) {
            System.out.printf("Sending message: %s to consumer %s\n", o.toString(), consumer.getClass());
            consumer.consume(o.toString());
        }
    }

    @Override
    public void add(Consumer consumer) {
        consumers.add(consumer);
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            String json = String.format("{ \"date\": \"%s\"}", new Date().toString());
            try {
                queue.put(json);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
