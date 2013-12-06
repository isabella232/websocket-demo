package com.xoom.research;

import java.util.HashSet;
import java.util.Set;

public class GraphiteProducer implements Producer {
    private final Set<Consumer> consumers = new HashSet<Consumer>();

    @Override
    public void start() {

    }

    @Override
    public void produce(Object o) {
        for (Consumer consumer : consumers) {
            consumer.consume(o);
        }
    }

    @Override
    public void add(Consumer consumer) {
        consumers.add(consumer);
    }
}
