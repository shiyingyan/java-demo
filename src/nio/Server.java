package nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private static final String stop = "end";

    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", 5454));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        while (true) {
            System.out.println("开始等待有通道可用");
            selector.select();//select是阻塞的，直到有一个或多个io通道可用，才返回。
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    SocketChannel client = serverSocket.accept();
                    System.out.println(client.getRemoteAddress().toString());
                    if (!key.isValid()) {
                        key.cancel();
                    }
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    client.read(buffer);

                    buffer.flip();
                    byte[] data = new byte[1024];
                    buffer.get(data, 0, buffer.limit());
                    buffer.clear();

                    String msg = new String(data).trim();
                    System.out.println("↓receive message:" + msg);
                    if (msg.equals(stop)) {
                        client.close();
                        System.out.println("Not acception client message anymore");
                    } else {
                        ByteBuffer rBuffer = ByteBuffer.allocate(1024);
                        rBuffer.put(msg.getBytes(), 0, msg.length());
                        System.out.println("↑send message:" + new String(rBuffer.array()));
                        rBuffer.flip();
                        client.write(rBuffer);
                        rBuffer.clear();
                    }

                }
                iterator.remove();
            }

        }
    }
}
