package com.zakl.msgdistribute;

import cn.hutool.core.lang.Pair;
import com.zakl.ack.AckCallBack;
import com.zakl.ack.AckHandlerManager;
import com.zakl.dto.MqMessage;
import com.zakl.protocol.MqSubMessage;
import com.zakl.redisinteractive.RedisUtil;
import com.zakl.statusManage.StatusManager;
import com.zakl.statusManage.SubClientInfo;
import com.zakl.util.MqHandleUtil;
import io.lettuce.core.ScoredValue;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import static com.zakl.protocol.MqSubMessage.TYPE_MQ_MESSAGE_PASSIVE_CALL;
import static com.zakl.statusManage.MqKeyHandleStatusManager.keyClientsMap;

/**
 * @author ZhangJiaKui
 * @classname MqMsgPassiveCallHandler
 * @description TODO
 * @date 6/8/2021 3:00 PM
 */

@Slf4j
public class MqMsgPassiveCallHandler {

    private final static PubMsgBufBufHandler bufBufHandler = PubMsgBufBufHandler.getInstance();

    public static void handlePassiveCall(ChannelHandlerContext ctx, MqSubMessage msg, SubClientInfo subClientInfo) {
        List<MqMessage> mqMessages = new ArrayList<>();
        List<Pair<String, Integer>> passiveCalls = msg.getPassiveCallKeys();
        for (Pair<String, Integer> passiveCall : passiveCalls) {
            String keyName = passiveCall.getKey();
            Integer cnt = passiveCall.getValue();
            if (!keyClientsMap.containsKey(keyName)) {
                log.info("init new passiveCall key:{}", keyName);
                StatusManager.initNewKey(keyName);
            }
            if (MqHandleUtil.checkIfSortedSet(keyName)) {
                mqMessages.addAll(priorityMsgPassiveCallHandle(keyName, cnt));
            } else if (MqHandleUtil.checkIfList(keyName)) {
                mqMessages.addAll(fifoMsgPassiveCallHandle(keyName, cnt));
            }
        }
        MqSubMessage responseMqMsg = new MqSubMessage();
        responseMqMsg.setType(TYPE_MQ_MESSAGE_PASSIVE_CALL);
        responseMqMsg.setPassiveCallId(msg.getPassiveCallId());
        responseMqMsg.setMqMessages(mqMessages);
        ctx.writeAndFlush(responseMqMsg);
        //todo 验证主动调用ack
        for (MqMessage mqMessage : mqMessages) {
            AckCallBack ackCallBack = new AckCallBack(mqMessage);
            AckHandlerManager.getClientAckHandler(subClientInfo).submitNewAckHandleRequest(ackCallBack);
        }
    }

    private static List<MqMessage> fifoMsgPassiveCallHandle(String keyName, Integer cnt) {
        List<MqMessage> ret = new ArrayList<>();
        if (cnt == 0) return ret;
        int tmtCnt = 0;
        List<String> redisMsgs = RedisUtil.syncListRPop(keyName, cnt);
        tmtCnt += redisMsgs.size();
        redisMsgs.forEach(msg -> ret.add(MqHandleUtil.convertRedisStringToMqMessage(keyName, -1, msg)));
        while (tmtCnt++ < cnt) {
            MqMessage mqMessage = bufBufHandler.listen(keyName);
            if (mqMessage == null) {
                break;
            }
            ret.add(mqMessage);
        }
        return ret;
    }

    private static List<MqMessage> priorityMsgPassiveCallHandle(String keyName, Integer cnt) {
        List<MqMessage> ret = new ArrayList<>();
        Queue<MqMessage> tmpPq = new PriorityQueue<>((o1, o2) -> Double.compare(o2.getWeight(), o1.getWeight()));
        List<ScoredValue<String>> scoredValues = RedisUtil.syncSortedSetPopMax(keyName, cnt);
        for (ScoredValue<String> sv : scoredValues) {
            tmpPq.offer(MqHandleUtil.convertRedisStringToMqMessage(keyName, sv.getScore(), sv.getValue()));
        }
        int tmpCnt = 0;
        while (tmpCnt++ < cnt) {
            MqMessage mqMessage = bufBufHandler.listen(keyName);
            if (mqMessage == null) {
                break;
            }
            tmpPq.offer(mqMessage);
        }
        while (ret.size() < cnt && !tmpPq.isEmpty()) {
            ret.add(tmpPq.poll());
        }
        if (!tmpPq.isEmpty()) {
            RedisUtil.syncSortedSetAdd(keyName, tmpPq.toArray(new MqMessage[0]));
        }
        return ret;
    }


}
