package com.zakl.nettyhandle;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.UUID;
import com.zakl.constant.Constants;
import com.zakl.dto.MqMessage;
import com.zakl.msgdistribute.PubMsgBufBufHandler;
import com.zakl.protocol.MqPubMessage;
import com.zakl.statusManage.MqKeyHandleStatusManager;
import com.zakl.statusManage.PubClientManager;
import com.zakl.statusManage.StatusManager;
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

        log.info("[" + ctx.channel().id() + "]" + new SimpleDateFormat("yyyy/MM/dd HH/mm/ss").format(new Date()) + "==>>>"
                + "channelRead0");
        if (msg.getType() == MqPubMessage.TYPE_PUBLISH) {
            handlePubMsgs(ctx, msg);
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        log.info("[" + ctx.channel().id() + "]" + new SimpleDateFormat("yyyy/MM/dd HH/mm/ss").format(new Date()) + "==>>>"
                + "channelActive");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("[" + ctx.channel().id() + "]" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "==>>>"
                + "channelInactive");
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    private void handlePubMsgs(ChannelHandlerContext ctx, MqPubMessage msg) {

        pubClientManager.pubClientMap.put(msg.getClientId(), ctx);

        log.info("receive pubMsg: {} from: {}", ctx, msg);

        Map<String, List<MqMessage>> keyMsgs = new HashMap<>();

        for (Map.Entry<String, List<Pair<Double, String>>> keyPusMsgs : msg.getPubMessages().entrySet()) {
            String keyName = keyPusMsgs.getKey();
            List<MqMessage> msgs = new ArrayList<>();
            if (!checkIfKeyValid(keyName)) {
                log.warn("keyName {} is illegal,please specify mqKey type SortedSet/List", keyName);
                continue;
            } else if (!MqKeyHandleStatusManager.keyClientsMap.containsKey(keyName)) {
                StatusManager.initNewKey(keyName);
            }
            for (Pair<Double, String> scoreAndValue : keyPusMsgs.getValue()) {
                double score = scoreAndValue.getKey();
                String value = scoreAndValue.getValue();
                MqMessage mqMessage = new MqMessage(UUID.randomUUID().toString(), score, keyName, value);
                msgs.add(mqMessage);
            }
            keyMsgs.put(keyName, msgs);
        }
        bufHandler.add(keyMsgs);

        doAckResponse(ctx, msg);
    }

    private void doAckResponse(ChannelHandlerContext ctx, MqPubMessage pubMsg) {
        MqPubMessage ackMsg = new MqPubMessage();
        ackMsg.setType(MqPubMessage.TYPE_ACK);
        ackMsg.setClientId(pubMsg.getClientId());
        ackMsg.setPubId(pubMsg.getPubId());
        ctx.writeAndFlush(ackMsg);
    }


}
