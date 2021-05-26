package com.zakl.container;

import com.zakl.config.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZhangJiaKui
 * @classname ServerContainer
 * @description TODO
 * @date 5/25/2021 2:15 PM
 */
@Slf4j
public class ServerContainer implements Container {

    private NioEventLoopGroup serverWorkerGroup;

    private NioEventLoopGroup serverBossGroup;

    public ServerContainer() {
        this.serverWorkerGroup = new NioEventLoopGroup();
        this.serverBossGroup = new NioEventLoopGroup();
    }

    @Override
    public void start() {
        {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(serverBossGroup, serverWorkerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast();
                    ch.pipeline().addLast();
                    ch.pipeline().addLast();
                    ch.pipeline().addLast();
                }
            });

            try {
                bootstrap.bind(ServerConfig.getInstance().getServerBind(), ServerConfig.getInstance().getServerPort()).get();
                log.info("proxy server start on port " + ServerConfig.getInstance().getServerPort());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

        }

    }

    @Override
    public void stop() {

    }
}
