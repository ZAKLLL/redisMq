package com.zakl.nettyhandle;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.UUID;
import com.zakl.dto.MqMessage;
import com.zakl.msgdistribute.PubMsgBufBufHandler;
import com.zakl.protocol.MqPubMessage;
import com.zakl.statusManage.PubClientManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.zakl.util.MqHandleUtil.checkIfKeyValid;

/**
 * @author ZhangJiaKui
 * @classname MqPubMessageHandler
 * @description TODO
 * @date 5/27/2021 3:05 PM
 */
@Slf4j
@ChannelHandler.Sharable
public class MqPubMessageHandler extends SimpleChannelInboundHandler<MqPubMessage> {

    private static final PubMsgBufBufHandler bufHandler = PubMsgBufBufHandler.getInstance();

    private static final PubClientManager pubClientManager = PubClientManager.getInstance();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqPubMessage msg) throws Exception {

        pubClientManager.pubClientMap.put(msg.getClientId(), ctx);

        log.info("receive pubMsg: {} from: {}", ctx, msg);

        Map<String, List<MqMessage>> keyMsgs = new HashMap<>();

        for (Map.Entry<String, List<Pair<Double, String>>> keyPusMsgs : msg.getPubMessages().entrySet()) {
            String key = keyPusMsgs.getKey();
            List<MqMessage> msgs = new ArrayList<>();
            if (!checkIfKeyValid(key)) {
                log.warn("keyName {} is illegal,please specify mqKey type SortedSet/List", key);
                continue;
            }
            for (Pair<Double, String> scoreAndValue : keyPusMsgs.getValue()) {
                double score = scoreAndValue.getKey();
                String value = scoreAndValue.getValue();
                MqMessage mqMessage = new MqMessage(UUID.randomUUID().toString(), score, key, value);
                msgs.add(mqMessage);
            }
            keyMsgs.put(key, msgs);
        }
        bufHandler.add(keyMsgs);
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
