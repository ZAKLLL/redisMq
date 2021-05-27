package com.zakl.container;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ContainerHelper {


    private static volatile boolean running = true;

    private static Container[] cachedContainers;

    public static void start(Container... containers) {

        cachedContainers = containers;

        // 启动所有容器
        startContainers();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {

                synchronized (ContainerHelper.class) {

                    // 停止所有容器.
                    stopContainers();
                    running = false;
                    ContainerHelper.class.notify();
                }
            }
        });

        synchronized (ContainerHelper.class) {
            while (running) {
                try {
                    ContainerHelper.class.wait();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void startContainers() {
        for (Container container : cachedContainers) {
            log.info("starting container [{}]", container.getClass().getName());
            container.start();
            log.info("container [{}] started", container.getClass().getName());
        }
    }

    private static void stopContainers() {
        for (Container container : cachedContainers) {
            log.info("stopping container [{}]", container.getClass().getName());
            try {
                container.stop();
                log.info("container [{}] stopped", container.getClass().getName());
            } catch (Exception ex) {
                log.warn("container stopped with error", ex);
            }
        }
    }
}
