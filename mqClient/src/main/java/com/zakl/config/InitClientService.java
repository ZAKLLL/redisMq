package com.zakl.config;

import com.zakl.MqServiceLoader;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;

/**
 * @author ZhangJiaKui
 * @classname InitClientService
 * @description TODO
 * @date 6/9/2021 3:50 PM
 */
@Slf4j
public class InitClientService {
    /**
     * InitClientService
     */
    public static void init() {
        ServiceLoader<MqServiceLoader> load = ServiceLoader.load(MqServiceLoader.class);
        for (MqServiceLoader clientService : load) {
            log.info("InitClientService: {}", clientService);
        }
    }
}
