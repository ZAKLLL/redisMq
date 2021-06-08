package com.zakl.nettyhandle;

import com.zakl.protocol.MqSubMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.zakl.msgdistribute.MqMsgDistributor.distributeMsgToCaller;
import static com.zakl.msgdistribute.MqMsgDistributor.distributeMsgToConsumeMethod;
import static com.zakl.msgdistribute.keyMethodManager.genMqSubscribeMsg;

/**
 * @author ZhangJiaKui
 * @classname MqSubMessageHandler
 * @description TODO
 * @date 5/27/2021 4:26 PM
 */
@Slf4j
public class MqSubMessageClientHandler extends SimpleChannelInboundHandler<MqSubMessage> {

    private static ChannelHandlerContext ctx;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqSubMessage msg) throws Exception {
        log.info("【" + ctx.channel().id() + "】" + new SimpleDateFormat("yyyy/MM/dd HH/mm/ss").format(new Date()) + "==>>>"
                + "channelRead: {}", msg);
        if (msg.getType() == MqSubMessage.TYPE_MQ_MESSAGE_ACTIVE_PUSH) {
            distributeMsgToConsumeMethod(msg);
        } else if (msg.getType() == MqSubMessage.TYPE_MQ_MESSAGE_PASSIVE_CALL) {
            distributeMsgToCaller(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("【" + ctx.channel().id() + "】" + new SimpleDateFormat("yyyy/MM/dd HH/mm/ss").format(new Date()) + "==>>>"
                + "channelActive");
        MqSubMessageClientHandler.ctx = ctx;
        log.info("start register cur client to MqServer ");
        MqSubMessage mqSubMessage = genMqSubscribeMsg();
        ctx.writeAndFlush(mqSubMessage);
        super.channelActive(ctx);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("【" + ctx.channel().id() + "】" + new SimpleDateFormat("yyyy/MM/dd HH/mm/ss").format(new Date()) + "==>>>"
                + "channelInactive");
        super.channelInactive(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public static ChannelHandlerContext getCtx() {
        return ctx;
    }
}
