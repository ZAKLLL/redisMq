package com.zakl.container;

import com.zakl.config.ServerConfig;
import com.zakl.nettyhandle.NettyHandlerHelper;
import com.zakl.protostuff.ProtostuffCodecUtil;
import com.zakl.protostuff.ProtostuffDecoder;
import com.zakl.protostuff.ProtostuffEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhangJiaKui
 * @classname ServerContainer
 * @description TODO
 * @date 5/25/2021 2:15 PM
 */
@Slf4j
@SuppressWarnings("all")
public class MqServerContainer implements Container {

    private final NioEventLoopGroup serverBossGroup;

    private final NioEventLoopGroup serverWorkerGroup;



    private final Class<?> msgType;

    private final static Map<Class<?>, Integer> msgPortMap = new HashMap<>();

    static {
        //初始化配置信息
        ServerConfig.init();
    }


    public static void regisMsgPort(Class<?> clazz, Integer port) {
        msgPortMap.put(clazz, port);
    }


    public MqServerContainer(NioEventLoopGroup serverBossGroup, Class msgType) {
        this.serverWorkerGroup = new NioEventLoopGroup();
        this.serverBossGroup = serverBossGroup;
        this.msgType = msgType;
    }


    @Override
    public void start() {
        {
            //用来接受Pub信息
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(serverBossGroup, serverWorkerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel ch) {
                    ProtostuffCodecUtil codecUtil = new ProtostuffCodecUtil(msgType);
                    ch.pipeline().addLast(new ProtostuffEncoder(codecUtil));
                    ch.pipeline().addLast(new ProtostuffDecoder(codecUtil)); //todo 添加心跳检测handler
                    ch.pipeline().addLast(NettyHandlerHelper.getSingletonHandler(msgType));
                }
            });

            if (!msgPortMap.containsKey(msgType)) {
                throw new RuntimeException(msgType.getSimpleName() + "无配置对应ServerPort");
            }

            //根据不同的消息类型,开启指定的端口
            Integer port = msgPortMap.get(msgType);

            try {
                serverBootstrap.bind(port).get();
                log.info("{} mq server start on {} ", msgType.getName(), port);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

        }

    }

    @Override
    public void stop() {
        serverWorkerGroup.shutdownGracefully();
    }
}
