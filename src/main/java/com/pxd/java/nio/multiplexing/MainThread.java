package com.pxd.java.nio.multiplexing;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class MainThread {

    SelectorThread[] selectorThreads;

    AtomicInteger xid = new AtomicInteger(0);

    ServerSocketChannel ssc;

    public MainThread(int num) {
        selectorThreads = new SelectorThread[num];
        for (int i = 0; i < num; i++) {
            selectorThreads[i] = new SelectorThread();
            new Thread(selectorThreads[i]).start();
        }
    }

    public void bind(int port) throws IOException {
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(port));

        nextSelector(ssc);

    }

    private void nextSelector(Channel c) {
        SelectorThread next = next();
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) c;

        try {
            serverSocketChannel.register(next.selector, SelectionKey.OP_ACCEPT);
            next.selector.wakeup();
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        }

    }

    private SelectorThread next() {
        int idx = xid.getAndIncrement() % selectorThreads.length;
        return selectorThreads[idx];
    }

    public static void main(String[] args) throws IOException {
        MainThread mainThread = new MainThread(1);
        mainThread.bind(8080);
    }

}
