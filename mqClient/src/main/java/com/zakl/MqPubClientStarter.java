package com.zakl;

import com.zakl.nettyhandle.MqClientContainer;

/**
 * @author ZhangJiaKui
 * @classname MqPubClientStarter
 * @description TODO
 * @date 6/4/2021 4:31 PM
 */
public class MqPubClientStarter {
    public static void main(String[] args) {
        new MqClientContainer(true).start();
    }
}

