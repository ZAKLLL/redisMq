package com.zakl;

import com.zakl.config.ClientServiceLoader;
import com.zakl.nettyhandle.MqClientContainer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZhangJiaKui
 * @classname MqPubClientStarter
 * @description TODO
 * @date 6/4/2021 4:31 PM
 */
@Slf4j
public class MqSubClientStarter implements ClientServiceLoader {

    static {
        log.info("load com.zakl.MqSubClientStarter");
        new MqClientContainer(false).start();
    }
}
