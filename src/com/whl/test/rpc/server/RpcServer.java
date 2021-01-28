package com.whl.test.rpc.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import com.whl.test.rpc.Hello;

public class RpcServer {
    private static final HashMap<Class<?>, Object> serviceRegistry = new HashMap<Class<?>, Object>();

    private InetSocketAddress address;

    public RpcServer(String ip, int port) {
        this.address = new InetSocketAddress(ip, port);
    }

    public RpcServer register(Class<?> serviceInterface, Object instance) {
        this.serviceRegistry.put(serviceInterface, instance);
        return this;
    }

    public void run() throws IOException {
        ServerSocket server = new ServerSocket();
        server.bind(this.address);
        System.out.println("server started.");
        Socket socket = null;

        while (true) {
            socket = server.accept();

            try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
                String serviceName = ois.readUTF();
                String methodName = ois.readUTF();
                Class<?>[] parameterTypes = (Class<?>[]) ois.readObject();
                Object[] arguments = (Object[]) ois.readObject();
                Class<?> serviceInterface = Class.forName(serviceName);
                Object instance = this.serviceRegistry.get(serviceInterface);

                if (instance == null) {
                    throw new RuntimeException("No provider available for the service " + serviceName);
                }
                Method method = serviceInterface.getMethod(methodName, parameterTypes);
                Object result = method.invoke(instance, arguments);
                oos.writeObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        RpcServer server = new RpcServer("127.0.0.1", 6666 );
        server.register(Hello.class, new HelloImpl());
        server.run();
    }

}
