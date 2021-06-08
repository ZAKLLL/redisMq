package com.zakl.msgdistribute;

import cn.hutool.core.collection.ListUtil;
import com.zakl.ack.AckCallBack;
import com.zakl.ack.AckHandlerManager;
import com.zakl.protocol.MqSubMessage;
import com.zakl.redisinteractive.RedisUtil;
import com.zakl.dto.MqMessage;
import com.zakl.statusManage.MqKeyHandleStatusManager;
import com.zakl.statusManage.SubClientInfo;
import io.lettuce.core.ScoredValue;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.PriorityBlockingQueue;

import static com.zakl.statusManage.StatusManager.cleanUpOffLiveSubClient;
import static com.zakl.statusManage.StatusManager.suspendDistributeThread;
import static com.zakl.util.MqHandleUtil.convertRedisStringToMqMessage;

@Slf4j
public class MsgDistributeHandler  {

    private final static MsgDistributeHandler instance = new MsgDistributeHandler();


    private MsgDistributeHandler() {
    }


    public static MsgDistributeHandler getInstance() {
        return instance;
    }

    public void distribute(MqMessage bufMsg, String keyName) {
        MqMessage msgToDistribute;
        ScoredValue<String> scoreValue = RedisUtil.syncSortedSetPopMax(keyName);
        if (scoreValue.hasValue() && bufMsg != null) {
            // buf区与redis中均有数据
            // 将此信息与RedisServer 中的 max value进行比对,如果当前信息优先级更高,则分发该信息
            MqMessage msgToRedis;

            MqMessage mqMessageFormRedis = convertRedisStringToMqMessage(keyName, scoreValue.getScore(), scoreValue.getValue());
            //比较权重
            if (bufMsg.getWeight() > scoreValue.getScore()) {
                //分发 bufMsg
                msgToRedis = bufMsg;
                msgToDistribute = mqMessageFormRedis;
            } else {
                msgToRedis = mqMessageFormRedis;
                msgToDistribute = bufMsg;
            }
            RedisUtil.syncSortedSetAdd(msgToRedis);
        } else if (bufMsg != null) {
            //redis中无数据
            msgToDistribute = bufMsg;
        } else if (scoreValue.hasValue()) {
            //buf 区无数据
            msgToDistribute = convertRedisStringToMqMessage(keyName, scoreValue.getScore(), scoreValue.getValue());
        } else {
            // redis 与 buf 区都无数据
            msgToDistribute = null;
        }
        doDistribute(msgToDistribute, keyName);
    }


    private static void doDistribute(MqMessage mqMessage, String keyName) {
        if (mqMessage == null) {
            log.info("key: {} is empty, suspend corresponding msgDistribute thread", keyName);
            suspendDistributeThread(keyName);
            return;
        }
        PriorityBlockingQueue<SubClientInfo> subClientInfos = MqKeyHandleStatusManager.keyClientsMap.get(mqMessage.getKey());

        if (subClientInfos.isEmpty()) {
            log.info("key: {} 's subClient queue is empty,mqMessage:{} will push back to Redis", mqMessage.getKey(), mqMessage);
            suspendDistributeThread(keyName);
            //no client, push msg Back to redis;
            RedisUtil.syncSortedSetAdd(mqMessage);
            return;
        }

        SubClientInfo subClientInfo = subClientInfos.poll();
        String clientId = subClientInfo.getClientId();
        if (!subClientInfo.isAlive) {
            log.info("subClient {} 离线,channel 信息{}", clientId, subClientInfo.getContext());
            //退回到redis
            RedisUtil.syncSortedSetAdd(mqMessage);
            cleanUpOffLiveSubClient(subClientInfo, keyName);
            return;
        }
        ChannelHandlerContext ctx = subClientInfo.getContext();


        MqSubMessage mqSubMessage = new MqSubMessage();
        mqSubMessage.setType(MqSubMessage.TYPE_MQ_MESSAGE);
        mqSubMessage.setMqMessages(ListUtil.toList(mqMessage));
        ctx.writeAndFlush(mqSubMessage);


        // ack handle non-blocking
        AckCallBack ackCallBack = new AckCallBack(mqMessage);
        AckHandlerManager.getClientAckHandler(subClientInfo).submitNewAckHandleRequest(ackCallBack);

    }
}
