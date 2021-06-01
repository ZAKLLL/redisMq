package com.zakl.nettyhandler;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.UUID;
import com.zakl.statusManage.PubClientManager;
import com.zakl.constant.Constants;
import com.zakl.dto.MqMessage;
import com.zakl.mqhandler.PriorityPubMsgBufBufHandler;
import com.zakl.mqhandler.PubMsgBufHandle;
import com.zakl.protocol.MqPubMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author ZhangJiaKui
 * @classname MqPubMessageHandler
 * @description TODO
 * @date 5/27/2021 3:05 PM
 */
@Slf4j
public class MqPubMessageHandler extends SimpleChannelInboundHandler<MqPubMessage> {

    private static final PubMsgBufHandle sBufHandler = PriorityPubMsgBufBufHandler.getInstance();
    private static final PubMsgBufHandle lBufHandler = PriorityPubMsgBufBufHandler.getInstance();

    private static final PubClientManager pubClientManager = PubClientManager.getInstance();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqPubMessage msg) throws Exception {
        //todo 是否在此处做心跳处理

        log.info("receive pubMsg: {} from: {}", ctx, msg);

        Map<String, List<MqMessage>> listKeyMsgs = new HashMap<>();
        Map<String, List<MqMessage>> sortedSetKeyMsgs = new HashMap<>();

        for (Map.Entry<String, List<Pair<Double, String>>> keyPusMsgs : msg.getPubMessages().entrySet()) {
            String key = keyPusMsgs.getKey();
            List<MqMessage> lMsgs = new ArrayList<>();
            List<MqMessage> sMsgs = new ArrayList<>();
            String lKey = Constants.MQ_LIST_PREFIX + key;
            String sKey = Constants.MQ_SORTED_SET_PREFIX + key;
            for (Pair<Double, String> scoreAndValue : keyPusMsgs.getValue()) {
                double score = scoreAndValue.getKey();
                String value = scoreAndValue.getValue();
                MqMessage mqMessage = new MqMessage(UUID.randomUUID().toString(), score, key, value);
                if (score == -1d) {
                    mqMessage.setKey(lKey);
                    lMsgs.add(mqMessage);
                } else {
                    mqMessage.setKey(sKey);
                    sMsgs.add(mqMessage);
                }
            }
            sortedSetKeyMsgs.put(sKey, sMsgs);
            listKeyMsgs.put(lKey, lMsgs);
        }
        lBufHandler.add(false, listKeyMsgs);
        sBufHandler.add(false, sortedSetKeyMsgs);
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
