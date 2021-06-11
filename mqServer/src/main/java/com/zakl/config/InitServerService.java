package com.zakl.config;

import com.zakl.MqServiceLoader;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;

@Slf4j
public class InitServerService {

    public static void init() {
        ServiceLoader<MqServiceLoader> load = ServiceLoader.load(MqServiceLoader.class);
        for (MqServiceLoader clientService : load) {
            log.info("InitClientService: {}", clientService);
        }
    }
}
