package com.zakl;

import cn.hutool.core.lang.Pair;
import com.zakl.config.ClientServiceLoader;
import com.zakl.consume.PassiveCaller;
import com.zakl.nettyhandle.MqClientContainer;
import com.zakl.publish.Publisher;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * @author ZhangJiaKui
 * @classname com.zakl.MqPubClientStarter
 * @description TODO
 * @date 6/4/2021 4:31 PM
 */
@Slf4j
public class MqPubClientStarter implements ClientServiceLoader {

//    static {
//        log.info("load com.zakl.MqPubClientStarter");
//        new MqClientContainer(true).start();
//    }

    public static void main(String[] args) {

        Random random = new Random();

        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                while (true) {
                    Thread.sleep(3000);
                    String[] keys = {"k1","k2","k3","k4"};
                    for (String key : keys) {
                        String value = "HelloWorld" + System.currentTimeMillis();
                        Publisher.publishToSortedSetKey(key,new Pair<>(random.nextDouble()*100,value));
                    }
                }
            }
        }).start();
        new MqClientContainer(true).start();

    }

}

