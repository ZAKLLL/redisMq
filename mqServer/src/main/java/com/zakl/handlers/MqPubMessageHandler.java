package com.zakl.handlers;

import com.zakl.protocol.MqPubMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ZhangJiaKui
 * @classname MqPubMessageHandler
 * @description TODO
 * @date 5/27/2021 3:05 PM
 */
@Slf4j
public class MqPubMessageHandler extends SimpleChannelInboundHandler<MqPubMessage> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqPubMessage msg) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("【" + ctx.channel().id() + "】" + new SimpleDateFormat("yyyy/MM/dd HH/mm/ss").format(new Date()) + "==>>>"
                + "channelActive");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("【" + ctx.channel().id() + "】" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "==>>>"
                + "channelInactive");
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
