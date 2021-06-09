package com.zakl.config;

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
        ServiceLoader<ClientServiceLoader> load = ServiceLoader.load(ClientServiceLoader.class);
        for (ClientServiceLoader clientService : load) {
            log.info("InitClientService: {}", clientService);
        }
    }
}
