package com.zakl.publish;

import cn.hutool.core.lang.Pair;
import com.zakl.config.ClientConfig;
import com.zakl.constant.Constants;
import com.zakl.msgpublish.PubAckCallBack;
import com.zakl.msgpublish.PubAckHandler;
import com.zakl.nettyhandle.MqPubMessageClientHandler;
import com.zakl.protocol.MqPubMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zakl.protocol.MqPubMessage.TYPE_PUBLISH;

/**
 * @author ZhangJiaKui
 * @classname Publisher
 * @description TODO
 * @date 6/9/2021 9:53 AM
 */
@Slf4j
public class Publisher {


    public static boolean publishToKey(Map<String, List<Pair<Double, String>>> keyValues) {
        log.info("publish msg:{} to server", keyValues);
        String pubId = UUID.randomUUID().toString();
        MqPubMessage mqPubMessage = new MqPubMessage();
        mqPubMessage.setPubId(pubId);
        mqPubMessage.setClientId(ClientConfig.getPubClientId());
        mqPubMessage.setType(TYPE_PUBLISH);
        mqPubMessage.setPubMessages(keyValues);
        MqPubMessageClientHandler.getContext().writeAndFlush(mqPubMessage);
        return PubAckHandler.submitAckCallBack(new PubAckCallBack(pubId));
    }

    @SafeVarargs
    public static boolean publishToSortedSetKey(String key, Pair<Double, String>... values) {
        final String sortedSetKey = Constants.MQ_SORTED_SET_PREFIX + key;
        Map<String, List<Pair<Double, String>>> keyValues = new HashMap<>();
        keyValues.put(sortedSetKey, Arrays.asList(values.clone()));
        return publishToKey(keyValues);
    }


    public static boolean publishToListKey(String key, String... values) {
        final String sortedSetKey = Constants.MQ_LIST_PREFIX + key;
        Map<String, List<Pair<Double, String>>> keyValues = new HashMap<String, List<Pair<Double, String>>>() {
            {
                List<Pair<Double, String>> scoredValues = Arrays.stream(values).flatMap(i -> Stream.of(new Pair<>(-1d, i))).collect(Collectors.toList());
                put(sortedSetKey, scoredValues);
            }
        };
        return publishToKey(keyValues);
    }


}
