package src;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
public class NioServer {
    private Selector selector;
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private ByteBuffer  writeBuffer= ByteBuffer.allocate(1024);
    public NioServer(int port) throws IOException {
        ServerSocketChannel serverSocketChannel=ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        ServerSocket serverSocket=serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(port));
        selector=Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("----Server Started----");
    }
    public void  run() throws IOException {
        int num = 0;
        while (true) {
            int selectKeyCount = selector.select();
            System.out.println(num++ + "selectCount:" + selectKeyCount);
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                processSelectedKey(selectionKey);
            }
        }
    }
    private void processSelectedKey(SelectionKey selectionKey) throws IOException {
        SocketChannel clientChannel = null;
        if (selectionKey.isAcceptable()) {
            ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
            clientChannel = server.accept();
            if (null == clientChannel) {
                return;
            }
            System.out.println("--- accepted clientChannel---");
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
        } else if (selectionKey.isReadable()) {
            clientChannel = (SocketChannel) selectionKey.channel();
            buffer.clear();
            int count = clientChannel.read(buffer); //从channel中读取
            if (count > 0) {
                String receiveContext = new String(buffer.array(), 0, count);
                System.out.println("receive client info:" + receiveContext);
            }
            writeBuffer.clear();
            clientChannel = (SocketChannel) selectionKey.channel();
            String sendContent = "hello client ,im server";
            writeBuffer.put(sendContent.getBytes());
            writeBuffer.flip();
            clientChannel.write(writeBuffer);//往channel里写
            System.out.println("send info to client:" + sendContent);
        }
    }
}
