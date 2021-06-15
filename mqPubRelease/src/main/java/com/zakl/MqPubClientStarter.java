package com.zakl;

import com.zakl.nettyhandle.MqClientContainer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZhangJiaKui
 * @classname com.zakl.MqPubClientStarter
 * @description TODO
 * @date 6/4/2021 4:31 PM
 */
@Slf4j
public class MqPubClientStarter implements MqServiceLoader {

    static {
        log.info("load com.zakl.MqPubClientStarter");
        new MqClientContainer(true).start();
    }
}

