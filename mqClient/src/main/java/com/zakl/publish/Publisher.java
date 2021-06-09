package com.zakl.publish;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.UUID;
import com.zakl.constant.Constants;
import com.zakl.nettyhandle.MqPubMessageClientHandler;
import com.zakl.protocol.MqPubMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final static String clientId = MqPubMessageClientHandler.getClientId();


    private static void publishToKey(Map<String, List<Pair<Double, String>>> keyValues) {
        log.info("publish msg:{} to server", keyValues);
        MqPubMessage mqPubMessage = new MqPubMessage();
        mqPubMessage.setClientId(clientId);
        mqPubMessage.setType(TYPE_PUBLISH);
        mqPubMessage.setPubMessages(keyValues);
        MqPubMessageClientHandler.getCtx().writeAndFlush(mqPubMessage);
    }

    @SafeVarargs
    public static void publishToSortedSetKey(String key, Pair<Double, String>... values) {
        final String sortedSetKey = Constants.MQ_SORTED_SET_PREFIX + key;
        Map<String, List<Pair<Double, String>>> keyValues= new HashMap<String, List<Pair<Double, String>>>() {
            {
                put(sortedSetKey, Arrays.asList(values.clone()));
            }
        };
        publishToKey(keyValues);
    }



    public static void publishToListKey(String key, String... values) {
        final String sortedSetKey = Constants.MQ_LIST_PREFIX + key;
        Map<String, List<Pair<Double, String>>> keyValues= new HashMap<String, List<Pair<Double, String>>>() {
            {
                List<Pair<Double, String>> scoredValues = Arrays.stream(values).flatMap(i -> Stream.of(new Pair<>(-1d, i))).collect(Collectors.toList());
                put(sortedSetKey, scoredValues);
            }
        };
        publishToKey(keyValues);
    }


}
