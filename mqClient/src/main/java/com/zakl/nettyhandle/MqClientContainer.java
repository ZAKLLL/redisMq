package com.zakl.nettyhandle;

import com.zakl.config.ClientConfig;
import com.zakl.container.Container;
import com.zakl.protocol.IdleCheckHandler;
import com.zakl.protocol.MqPubMessage;
import com.zakl.protocol.MqSubMessage;
import com.zakl.protocol.SupMqMessage;
import com.zakl.protostuff.ProtostuffCodecUtil;
import com.zakl.protostuff.ProtostuffDecoder;
import com.zakl.protostuff.ProtostuffEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author ZhangJiaKui
 * @classname MqSubClientContainer
 * @description TODO
 * @date 5/27/2021 4:11 PM
 */
@Slf4j
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

        log.info("start a {} client", isPub ? "publish" : "subscribe");
        Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) {
                Class<? extends SupMqMessage> mqMsgType = isPub ? MqPubMessage.class : MqSubMessage.class;

                ProtostuffCodecUtil msgCodec = new ProtostuffCodecUtil(mqMsgType);
                ch.pipeline().addLast(new ProtostuffEncoder(msgCodec));
                ch.pipeline().addLast(new ProtostuffDecoder(msgCodec));
                ch.pipeline().addLast(new IdleCheckHandler(IdleCheckHandler.READ_IDLE_TIME, IdleCheckHandler.WRITE_IDLE_TIME, 0, mqMsgType, ClientConfig.getClientId()));
                ch.pipeline().addLast(NettyHandlerHelper.getSingletonHandler(mqMsgType));
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
}
