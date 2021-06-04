package com.zakl.mqhandler;

import com.zakl.dto.MqMessage;
import com.zakl.nettyhandle.MqSubMessageClientHandler;
import com.zakl.protocol.MqPubMessage;
import com.zakl.protocol.MqSubMessage;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashSet;
import java.util.Set;

/**
 * ACK 处理器
 */
public class AckClientHandler {


    private boolean flag = false;

    private final byte ackType;

    public AckClientHandler(byte type) {
        ackType = type;
    }

    public void confirm(MqMessage... msgs) {
        if (flag) {
            throw new RuntimeException("ack confirm done ,don't do it again!");
        }
        MqSubMessage mqSubMessage = new MqSubMessage();
        mqSubMessage.setType(ackType);
        Set<String> ackIdSet = new HashSet<>();
        for (MqMessage msg : msgs) {
            ackIdSet.add(msg.getMessageId());
        }
        mqSubMessage.setAckMsgIdSet(ackIdSet);
        ChannelHandlerContext ctx = MqSubMessageClientHandler.getCtx();
        if (ctx == null) {
            throw new RuntimeException("disconnect with mqServer!");
        }
        ctx.writeAndFlush(mqSubMessage);
        flag = true;
    }


}
