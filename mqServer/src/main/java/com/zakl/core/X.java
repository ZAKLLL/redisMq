package com.zakl.core;


import cn.hutool.core.lang.Pair;
import com.zakl.protocol.MqSubMessage;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class X {

    private final static Set<String> queueSet = new CopyOnWriteArraySet<>();

    private final static Map<String, PriorityBlockingQueue<SubClientInfo>> sortedSetClientMap = new ConcurrentHashMap<>(16);

    private final static Map<String, SubClientInfo> clientMap = new ConcurrentHashMap<>(16);

    private final static Double MIN_SCORE = Double.MIN_VALUE;
    private final static String KEY_HOLDER = "holder";


    private final static ExecutorService executors = Executors.newCachedThreadPool();


    private final static Map<String, Condition> sortedSetHandleMap = new ConcurrentHashMap<>();
    private final static Map<String, AtomicBoolean> clientAliveMap = new ConcurrentHashMap<>();
    private final static ReentrantLock reentrantLock = new ReentrantLock();


    /**
     * 需要服务端主动推送消息的 客户端
     */
    private final static Set<String> initiativeClients = new CopyOnWriteArraySet<>();

    /**
     * 主动向服务端调用的获取消息的客户端
     */
    private final static Set<String> passiveClients = new CopyOnWriteArraySet<>();

    /**
     * 优先级队列
     */
    private final static Set<String> priorityKeys = new HashSet<>();

    /**
     * 先进先出队列
     */
    private final static Set<String> fifoKeys = new HashSet<>();


    public static void registerSubClient(ChannelHandlerContext ctx, MqSubMessage msg) {
        for (final String sortedSetChannelName : msg.getChannels()) {
            if (!queueSet.contains(sortedSetChannelName)) {
                log.info("start register new sorted Set in redis");

                registerNewSortedSet(sortedSetChannelName);

                SubClientInfo subClientInfo = new SubClientInfo(msg.getClientWeight(), ctx);
                PriorityBlockingQueue<SubClientInfo> subClientInfos = new PriorityBlockingQueue<>(16, Comparator.comparing(SubClientInfo::getWeight).reversed());
                sortedSetClientMap.computeIfAbsent(sortedSetChannelName, v -> subClientInfos).add(subClientInfo);

                clientMap.put(msg.getClientId(), subClientInfo);
                log.info("start register new sorted Set in redis successEd ");
                queueSet.add(sortedSetChannelName);
                Condition condition = reentrantLock.newCondition();

                sortedSetHandleMap.put(sortedSetChannelName, condition);
                AtomicBoolean clientAlive = new AtomicBoolean(true);

                clientAliveMap.put(sortedSetChannelName, clientAlive);


                //只监听
                executors.submit(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            while (clientAlive.get()) {
                                handleRcvAndDistribute(sortedSetChannelName);
                            }
                            try {
                                condition.await();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
    }


    public static void handleRcvAndDistribute(String keyName) {
        PubMsgBufHandle msgBufHandler = fifoKeys.contains(keyName) ? PriorityPubMsgBufBufHandler.getInstance() : FifoPubMsgBufBufHandler.getInstance();

//      MqPubMessage listen = PubMessageBufHandler.listen(sortedSetChannelName);
//                                DistributeMsgHandler.distributePubMsg(listen);

    }

    /**
     * 注册一个新的队列到Redis
     *
     * @param channelName
     */
    public static void registerNewSortedSet(String channelName) {

        PriorityPubMsgBufBufHandler.registerNewSortedSetBuf(channelName);
        RedisUtil.syncSortedSetAdd(channelName, new Pair<>(MIN_SCORE, KEY_HOLDER + channelName));
    }


}

@Data
@AllArgsConstructor
class SubClientInfo {
    Integer weight;
    ChannelHandlerContext context;

}
