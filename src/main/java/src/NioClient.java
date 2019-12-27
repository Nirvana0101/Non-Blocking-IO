package src;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NioClient {

    private static ByteBuffer buffer = ByteBuffer.allocate(1024);
    private static ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        SelectionKey selectionKey = socketChannel.register(selector, 0);
        boolean isConnected = socketChannel.connect(new InetSocketAddress("127.0.0.1", 7001));
        if (!isConnected) {
            selectionKey.interestOps(SelectionKey.OP_CONNECT);
        }
        int num = 0;
        while (true) {
            int selectCount = selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            SocketChannel client;
            while (iterator.hasNext()) {
                selectionKey = iterator.next();
                iterator.remove();
                int readyOps = selectionKey.readyOps();
                if ((readyOps & SelectionKey.OP_CONNECT) != 0) {
                    client = (SocketChannel) selectionKey.channel();
                    if (!client.finishConnect()) {
                        throw new Error();
                    }
                    System.out.println("--- client already connected----");
                    writeBuffer.clear();
                    writeBuffer.put("hello server,im a client".getBytes());
                    writeBuffer.flip();
                    client.write(writeBuffer);
                    selectionKey.interestOps(SelectionKey.OP_READ);
                } else if ((readyOps & SelectionKey.OP_READ) != 0) {
                    client = (SocketChannel) selectionKey.channel();
                    buffer.clear();
                    int count = client.read(buffer);
                    if (count > 0) {
                        String temp = new String(buffer.array(), 0, count);
                        System.out.println(num++ + "receive from server:" + temp);
                    }
                }
            }
        }
    }
}

