package com.zakl.nettyhandle;

import com.zakl.msgpublish.PubAckHandler;
import com.zakl.protocol.MqPubMessage;
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
public class MqPubMessageClientHandler extends SimpleChannelInboundHandler<MqPubMessage> {

    private static ChannelHandlerContext context;



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqPubMessage msg) throws Exception {
        log.info("[" + ctx.channel().id() + "]" + new SimpleDateFormat("yyyy/MM/dd HH/mm/ss").format(new Date()) + "==>>>"
                + "channelRead0");
        if (msg.getType() == MqPubMessage.TYPE_ACK) {
            PubAckHandler.confirm(msg);
        }
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

}
