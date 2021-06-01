package com.zakl.statusManage;

import com.zakl.mqhandler.MqHandleUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZhangJiaKui
 * @classname StatusManager
 * @description
 * @date 6/1/2021 5:22 PM
 */
public class StatusManager {

    public static Map<String, Boolean> statusMap = new ConcurrentHashMap<>();


    public static void initNewKey(String keyName) {


        if (MqHandleUtil.checkIfSortedSet(keyName)) {

        } else {

        }

    }

}
