package com.whl.test.rpc.server;

import com.whl.test.rpc.Hello;

class HelloImpl implements Hello {

    @Override
    public String sayHello(String username) {
        return "Hello, " + username;
    }

}
