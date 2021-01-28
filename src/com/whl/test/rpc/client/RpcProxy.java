package com.whl.test.rpc.client;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.whl.test.rpc.Hello;

public class RpcProxy<T> implements InvocationHandler {
    private Class<T> serviceInterface;

    private InetSocketAddress address;

    public RpcProxy(Class<T> serviceInterface, String ip, int port) {
        this.serviceInterface = serviceInterface;
        this.address = new InetSocketAddress(ip, port);
    }

    public T getClientInstance() {
        return (T) Proxy.newProxyInstance(this.serviceInterface.getClassLoader(), new Class<?>[]{this.serviceInterface}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Socket socket = null;

        try {
            // 创建Socket客户端，根据指定地址连接远程服务提供者
            socket = new Socket();
            socket.connect(this.address);

            try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
                // 将远程服务调用所需的接口类、方法名、参数列表等编码后发送给服务提供者
                oos.writeUTF(this.serviceInterface.getName());
                oos.writeUTF(method.getName());
                oos.writeObject(method.getParameterTypes());
                oos.writeObject(args);

                // 同步阻塞等待服务器返回应答，获取应答后返回
                return ois.readObject();
            }
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public static void main(String[] args) {
        RpcProxy<Hello> client = new RpcProxy<Hello>(Hello.class, "127.0.0.1", 6666);
        Hello hello = (Hello) client.getClientInstance();
        System.out.println(hello.sayHello("World"));
        System.out.println(hello.sayHello("wanghl"));
    }

}
