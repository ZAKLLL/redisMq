package com.zakl.mqhandler;

import cn.hutool.core.util.StrUtil;
import com.zakl.constant.Constants;
import com.zakl.dto.MqMessage;
import com.zakl.util.JsonUtils;

/**
 * @author ZhangJiaKui
 * @classname mqHandleUtil
 * @description TODO
 * @date 6/1/2021 5:09 PM
 */
public class MqHandleUtil {

    /**
     * Check what type the key is
     *
     * @param keyName
     * @return
     */
    public static boolean checkIfSortedSet(String keyName) {
        return keyName.startsWith(Constants.MQ_SORTED_SET_PREFIX);
    }

    public static boolean checkIfList(String keyName) {
        return keyName.startsWith(Constants.MQ_SORTED_SET_PREFIX);
    }

    public static boolean checkIfKeyValid(String keyName){
        return checkIfSortedSet(keyName)||checkIfList(keyName);
    }


    /**
     * convertMqMessageToJsonDate
     *
     * @param msg
     * @return
     */
    public static String convertMqMessageToJsonDate(MqMessage msg) {
        return JsonUtils.objectToJson(msg);
    }

    /**
     * convertJsonToMqMessage
     *
     * @param json
     * @return
     */
    public static MqMessage convertJsonToMqMessage(String json) {
        return JsonUtils.jsonToObject(json, MqMessage.class);
    }


    public static String convertMqMessageToRedisString(MqMessage msg) {
        return String.format("uuid:%s\n%s", msg.getMessageId(), msg.getMessage());
    }

    public static MqMessage convertRedisStringToMqMessage(String keyName, double score, String redisMsg) {
        if (StrUtil.isEmpty(redisMsg)) {
            return null;
        }
        //第一行 为uuid
        String[] uuidAndData = redisMsg.split("\n", 2);
        String uuid = uuidAndData[0];
        String data = uuidAndData[1];
        return new MqMessage(uuid.split(":")[1], score,keyName, data);
    }

}
