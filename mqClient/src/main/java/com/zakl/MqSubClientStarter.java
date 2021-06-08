package com.zakl;

import cn.hutool.core.lang.Pair;
import com.zakl.constant.Constants;
import com.zakl.dto.MqMessage;
import com.zakl.nettyhandle.MqClientContainer;
import com.zakl.passivecall.PassiveCaller;
import lombok.SneakyThrows;

import java.util.List;

/**
 * @author ZhangJiaKui
 * @classname MqPubClientStarter
 * @description TODO
 * @date 6/4/2021 4:31 PM
 */
public class MqSubClientStarter {
    public static void main(String[] args) {
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                Thread.sleep(5000);
                System.out.println("start request");
                while (true) {
                    List<MqMessage> messages = PassiveCaller.doConsume(true, new Pair<>("k1", 2));
                    for (MqMessage message : messages) {
                        System.out.println(message);
                    }
                    Thread.sleep(2000);
                }
            }
        }).start();
        new MqClientContainer(false).start();
    }
}
