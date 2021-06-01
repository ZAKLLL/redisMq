package com.zakl.mqhandler;

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

    public static boolean checkIfSortedSet(String keyName) {
        return keyName.startsWith(Constants.MQ_SORTED_SET_PREFIX);
    }


    public static String convertMqMessageToJsonDate(MqMessage msg) {
        return JsonUtils.objectToJson(msg);
    }

    public static MqMessage convertJsonToMqMessage(String json) {
        return JsonUtils.jsonToObject(json, MqMessage.class);
    }

}
