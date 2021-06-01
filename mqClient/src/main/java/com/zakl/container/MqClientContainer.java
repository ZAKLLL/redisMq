package com.zakl.container;

import com.zakl.config.ClientConfig;
import com.zakl.nettyhandler.MqPubMessageHandler;
import com.zakl.nettyhandler.MqSubMessageHandler;
import com.zakl.protocol.MqSubMessage;
import com.zakl.protostuff.ProtostuffCodecUtil;
import com.zakl.protostuff.ProtostuffDecoder;
import com.zakl.protostuff.ProtostuffEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * @author ZhangJiaKui
 * @classname MqSubClientContainer
 * @description TODO
 * @date 5/27/2021 4:11 PM
 */
public class MqClientContainer implements Container {
    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    /**
     * msg方向 true代表 publish 数据, false代表 subscribe数据
     */
    private final Boolean isPub;

    public MqClientContainer(boolean isPub) {
        this.isPub = isPub;
    }

    @Override
    public void start() {

        Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ProtostuffCodecUtil msgCodec = new ProtostuffCodecUtil(isPub ? MqPubMessage.class : MqSubMessage.class);
                ch.pipeline().addLast(new ProtostuffEncoder(msgCodec));
                ch.pipeline().addLast(new ProtostuffDecoder(msgCodec)); //todo 添加心跳检测handler
                ch.pipeline().addLast(isPub ? new MqPubMessageHandler() : new MqSubMessageHandler());
            }
        });

        //获取MqServer 的 address
        String serverIp = ClientConfig.getServerIp();
        Integer port = isPub ? ClientConfig.getMqPubPort() : ClientConfig.getMqSubPort();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(serverIp, port);
        b.connect(inetSocketAddress);
    }

    @Override
    public void stop() {
        eventLoopGroup.shutdownGracefully();
    }

    public static void main(String[] args) {
        new MqClientContainer(true).start();
        new MqClientContainer(false).start();
    }
}
