package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client implements Runnable {

    private SocketChannel socketChannel;
    private ByteBuffer byteBuffer;

    @Override
    public void run() {
        try {
            sendMessage("hello");
            sendMessage("world");
            sendMessage("end");
            stop();
        } catch (InterruptedException ie) {
            System.out.format("线程:%s被中断", Thread.currentThread().getName());
            ie.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Client newInstance() throws Exception {
        Client client = new Client();
        client.socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 5454));
        client.byteBuffer = ByteBuffer.allocate(1024);
        return client;
    }

    public void stop() throws Exception {
        System.out.println("发送消息完成，开始清理相关资源");
        socketChannel.close();
        byteBuffer = null;
    }

    public void sendMessage(String message) {
        String name = Thread.currentThread().getName();
        System.out.println(new StringBuilder().append("线程:").append(name).append(",开始发送消息：").append(message).toString());
        byteBuffer = ByteBuffer.wrap(message.getBytes());
        try {
            socketChannel.write(byteBuffer);
            byteBuffer.clear();
            socketChannel.read(byteBuffer);
            String response = new String(byteBuffer.array()).trim();
            System.out.println(new StringBuilder("线程:").append(name).append(",收到消息:").append(response).toString());
            byteBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        int clientCount = 10;
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < clientCount; i++) {
            pool.submit(Client.newInstance());
        }

        Thread.sleep(1000 * 10);
        pool.shutdown();
        System.out.println("主线程已停止");
    }
}
