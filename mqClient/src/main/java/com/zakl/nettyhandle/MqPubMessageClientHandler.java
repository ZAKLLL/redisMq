package com.zakl.nettyhandle;

import cn.hutool.core.lang.UUID;
import com.zakl.protocol.MqSubMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author ZhangJiaKui
 * @classname MqSubMessageHandler
 * @description TODO
 * @date 5/27/2021 4:26 PM
 */
@Slf4j
public class MqPubMessageClientHandler extends SimpleChannelInboundHandler<MqSubMessage> {

    private static ChannelHandlerContext context;

    private final static String clientId = UUID.randomUUID().toString();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqSubMessage msg) throws Exception {
        log.info("[" + ctx.channel().id() + "]" + new SimpleDateFormat("yyyy/MM/dd HH/mm/ss").format(new Date()) + "==>>>"
                + "channelRead0");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("[" + ctx.channel().id() + "]" + new SimpleDateFormat("yyyy/MM/dd HH/mm/ss").format(new Date()) + "==>>>"
                + "channelActive");
        context = ctx;
        super.channelActive(ctx);
    }

    public static ChannelHandlerContext getContext() {
        return context;
    }

    public static String getClientId() {
        return clientId;
    }
}
