package com.pxd.java.nio.multiplexing;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class SelectorThread implements Runnable {

    public Selector selector;

    public SelectorThread() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {

                int selectCount = selector.select();
                if (selectCount > 0) {
                    System.out.println(Thread.currentThread().getName() + " before selectKeys, selectCount = " + selectCount);
                    Set<SelectionKey> keys = selector.selectedKeys();
                    System.out.println(Thread.currentThread().getName() + " after selectKeys, selectCount = " + selectCount);
                    Iterator<SelectionKey> keyIterator = keys.iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();

                        if (key.isAcceptable()) {
                            handleAccept(key);

                        } else if (key.isReadable()) {
                            handleRead(key);

                        } else if (key.isWritable()) {
                            handleWrite(key);
                            
                        }

                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleWrite(SelectionKey key) {
    }

    private void handleRead(SelectionKey key) {
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel socketChannel = (SocketChannel) key.channel();
        buffer.clear();
        while (true) {
            try {
                int num = socketChannel.read(buffer);
                if (num > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        socketChannel.write(buffer);
                    }
                    buffer.clear();
                } else if (num == 0) {
                    System.out.println("nothing to read");
                    break;
                } else {
                    System.out.println("read error");
                    key.cancel();
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleAccept(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);



        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
