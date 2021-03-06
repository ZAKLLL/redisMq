package com.zakl.msgdistribute;

import cn.hutool.core.collection.ListUtil;
import com.zakl.ack.AckCallBack;
import com.zakl.ack.AckHandlerManager;
import com.zakl.dto.MqMessage;
import com.zakl.protocol.MqSubMessage;
import com.zakl.redisinteractive.RedisUtil;
import com.zakl.statusManage.MqKeyHandleStatusManager;
import com.zakl.statusManage.SubClientInfo;
import com.zakl.util.MqHandleUtil;
import io.lettuce.core.ScoredValue;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.PriorityBlockingQueue;

import static com.zakl.statusManage.StatusManager.cleanUpOffLiveSubClient;
import static com.zakl.statusManage.StatusManager.suspendDistributeThread;
import static com.zakl.util.MqHandleUtil.convertRedisStringToMqMessage;

@Slf4j
public class MsgDistributeHandler {

    private final static MsgDistributeHandler instance = new MsgDistributeHandler();


    private MsgDistributeHandler() {
    }


    public static MsgDistributeHandler getInstance() {
        return instance;
    }

    public void distribute(MqMessage bufMsg, String keyName) {
        if (MqHandleUtil.checkIfSortedSet(keyName)) {
            priorityDistribute(bufMsg, keyName);
        } else if (MqHandleUtil.checkIfList(keyName)) {
            listDistribute(bufMsg, keyName);
        }
    }

    private void listDistribute(MqMessage bufMsg, String keyName) {
        MqMessage msgToDistribute = null;
        String msgFromRedis = RedisUtil.syncListRPop(keyName);
        if (msgFromRedis != null) {
            msgToDistribute = MqHandleUtil.convertRedisStringToMqMessage(keyName, -1, msgFromRedis);
            if (bufMsg != null) {
                RedisUtil.syncListRPush(bufMsg);
            }
        } else if (bufMsg != null) {
            msgToDistribute = bufMsg;
        }
        if (msgToDistribute != null) {
            doDistribute(msgToDistribute, keyName);
        }
    }

    private void priorityDistribute(MqMessage bufMsg, String keyName) {
        MqMessage msgToDistribute;
        ScoredValue<String> scoreValue = RedisUtil.syncSortedSetPopMax(keyName);
        log.info("sorted set: {} data from redis :{}", keyName, scoreValue);
        if (scoreValue.hasValue() && bufMsg != null) {
            // buf??????redis???????????????
            // ???????????????RedisServer ?????? max value????????????,?????????????????????????????????,??????????????????
            MqMessage msgToRedis;

            MqMessage mqMessageFormRedis = convertRedisStringToMqMessage(keyName, scoreValue.getScore(), scoreValue.getValue());
            //????????????
            if (bufMsg.getWeight() > scoreValue.getScore()) {
                //?????? bufMsg
                msgToRedis = bufMsg;
                msgToDistribute = mqMessageFormRedis;
            } else {
                msgToRedis = mqMessageFormRedis;
                msgToDistribute = bufMsg;
            }
            RedisUtil.syncSortedSetAdd(msgToRedis);
        } else if (bufMsg != null) {
            //redis????????????
            msgToDistribute = bufMsg;
        } else if (scoreValue.hasValue()) {
            //buf ????????????
            msgToDistribute = convertRedisStringToMqMessage(keyName, scoreValue.getScore(), scoreValue.getValue());
        } else {
            // redis ??? buf ???????????????
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
            log.info("subClient {} ??????,channel ??????{}", clientId, subClientInfo.getContext());
            //?????????redis
            RedisUtil.syncSortedSetAdd(mqMessage);
            cleanUpOffLiveSubClient(subClientInfo, keyName);
            return;
        }
        ChannelHandlerContext ctx = subClientInfo.getContext();


        MqSubMessage mqSubMessage = new MqSubMessage();
        mqSubMessage.setType(MqSubMessage.TYPE_MQ_MESSAGE_ACTIVE_PUSH);
        mqSubMessage.setMqMessages(ListUtil.toList(mqMessage));


        // ack handle non-blocking
        AckCallBack ackCallBack = new AckCallBack(mqMessage);
        AckHandlerManager.getClientAckHandler(subClientInfo).submitNewAckHandleRequest(ackCallBack);

        ctx.writeAndFlush(mqSubMessage);

    }
}
