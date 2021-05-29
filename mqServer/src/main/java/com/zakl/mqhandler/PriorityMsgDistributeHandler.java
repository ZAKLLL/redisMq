package com.zakl.mqhandler;

import com.zakl.connection.SubClientInfo;
import com.zakl.connection.SubClientManager;
import com.zakl.dto.MqMessage;
import io.lettuce.core.ScoredValue;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Condition;

import static com.zakl.connection.SubClientManager.sortedSetHandleConditionMap;

@Slf4j
public class PriorityMsgDistributeHandler implements MqMsgDistributeHandle {

    private final static PriorityMsgDistributeHandler instance = new PriorityMsgDistributeHandler();

    private PriorityMsgDistributeHandler() {

    }


    public static MqMsgDistributeHandle getInstance() {
        return instance;
    }

    @Override
    public void distribute(MqMessage bufMsg, String keyName) {
        MqMessage msgToDistribute;
        ScoredValue<String> scoreValue = RedisUtil.syncSortedSetPopMax(keyName);
        if (scoreValue.hasValue() && bufMsg != null) {
            // buf区与redis中均有数据
            // 将此信息与RedisServer 中的 max value进行比对,如果当前信息优先级更高,则分发该信息
            MqMessage msgToRedis;

            MqMessage mqMessageFormRedis = convertScoredValueToMqMessage(scoreValue, keyName);
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
            msgToDistribute = convertScoredValueToMqMessage(scoreValue, keyName);
        } else {
            // redis 与 buf 区都无数据
            msgToDistribute = null;
        }
        doDistribute(msgToDistribute);
    }


    /**
     * 分发消息并且等到Ack响应之后,才会丢弃该信息
     *
     * @param mqMessage
     */
    private static void doDistribute(MqMessage mqMessage) {
        if (mqMessage == null) return;
        PriorityBlockingQueue<SubClientInfo> subClientInfos = SubClientManager.sortedSetClientMap.get(mqMessage.getKey());

        if (subClientInfos.isEmpty()) {
            throw new RuntimeException("无法正常获取连接key对应的subClient");
        }

        SubClientInfo subClientInfo = subClientInfos.poll();
        String clientId = subClientInfo.getClientId();
        if (!SubClientManager.clientAliveMap.get(clientId).get()) {
            log.info("subClient {} 离线,channel 信息{}", clientId, subClientInfo.getContext());
            RedisUtil.syncSortedSetAdd(mqMessage);
            Condition condition = sortedSetHandleConditionMap.get(clientId);

//            condition
            return;
        }
        ChannelHandlerContext ctx = subClientInfo.getContext();


        //todo Ack机制实现
        //

    }


    private static MqMessage convertScoredValueToMqMessage(ScoredValue<String> scoreValue, String keyName) {
        if (!scoreValue.hasValue()) return null;
        String redisMsg = scoreValue.getValue();
        //第一行 为uuid
        String[] uuidAndData = redisMsg.split("\n", 2);
        String uuid = uuidAndData[0];
        String data = uuidAndData[1];
        return new MqMessage(uuid.split(":")[1], scoreValue.getScore(), keyName, data);
    }

}