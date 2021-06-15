package com.zakl.nettyhandle;

import com.zakl.ack.AckCallBack;
import com.zakl.ack.AckResponseHandler;
import com.zakl.constant.Constants;
import com.zakl.protocol.MqSubMessage;
import com.zakl.statusManage.MqKeyHandleStatusManager;
import com.zakl.statusManage.StatusManager;
import com.zakl.statusManage.SubClientInfo;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.zakl.msgdistribute.MqMsgPassiveCallHandler.handlePassiveCall;
import static com.zakl.statusManage.MqKeyHandleStatusManager.clientIdMap;
import static com.zakl.statusManage.StatusManager.cleanUpOffLiveSubClient;
import static com.zakl.statusManage.StatusManager.remindDistributeThreadConsume;

/**
 * @author ZhangJiaKui
 * @classname MqSubMessageHandler
 * @description TODO
 * @date 5/27/2021 3:05 PM
 */
@Slf4j
@ChannelHandler.Sharable
public class MqSubMessageHandler extends SimpleChannelInboundHandler<MqSubMessage> {
    private final static Map<ChannelHandlerContext, SubClientInfo> ctxClientMap = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqSubMessage msg) throws Exception {
        log.info("receive mqSubMsg from client: {}, msg: {}", msg.getClientId(), msg);
        byte type = msg.getType();
        switch (type) {
            case MqSubMessage.TYPE_SUBSCRIBE: {
                registerSubClient(ctx, msg);
                break;
            }
            case MqSubMessage.PASSIVE_CALL: {
                handlePassiveCall(ctx, msg, ctxClientMap.get(ctx));
                break;
            }
            case MqSubMessage.TYPE_ACK_AUTO:
            case MqSubMessage.TYPE_ACK_MANUAL: {
                ackResponseHandle(msg.getAckMsgIdSet(), type);
                break;
            }
            default: {
                break;
            }
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
        log.info("[" + ctx.channel().id() + "]" + new SimpleDateFormat("yyyy/MM/dd HH/mm/ss").format(new Date()) + "==>>>"
                + "channelInactive");
        cleanSubClientInfo(ctx);
        super.channelInactive(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void registerSubClient(ChannelHandlerContext ctx, MqSubMessage msg) {

        SubClientInfo subClientInfo = new SubClientInfo(msg.getClientId(), msg.getClientWeight(), ctx);

        log.info("register new client: {}", subClientInfo);

        clientIdMap.put(msg.getClientId(), subClientInfo);
        ctxClientMap.put(ctx, subClientInfo);
        for (final String keyName : msg.getActivePushKeys()) {
            if (!MqKeyHandleStatusManager.keyClientsMap.containsKey(keyName)) {
                StatusManager.initNewKey(keyName, subClientInfo);
            } else {
                MqKeyHandleStatusManager.keyClientsMap.get(keyName).offer(subClientInfo);
            }
            //remind distribute thread work
            remindDistributeThreadConsume(keyName);
        }
    }

    private void cleanSubClientInfo(ChannelHandlerContext ctx) {
        SubClientInfo clientInfo = ctxClientMap.remove(ctx);
        log.info("remove offLine subClient: {},ctx: {}", clientInfo, ctx);
        cleanUpOffLiveSubClient(clientInfo);
    }


    private void ackResponseHandle(Set<String> ackMsgIdSet, byte type) {
        for (String msgId : ackMsgIdSet) {
            log.info("messageId:{} receive Ack ,type:{}", msgId, type);
            AckCallBack ackCallBack = AckResponseHandler.ackCallBackMap.get(msgId);
            if (ackCallBack == null) {
                log.info("not ackCallBack for current msgId: {}", msgId);
                return;
            }
            ackCallBack.over(type);
        }
    }

}
