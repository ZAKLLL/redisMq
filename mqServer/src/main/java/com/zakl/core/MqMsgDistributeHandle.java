package com.zakl.core;

import com.zakl.dto.MqMessage;

/**
 * @author ZhangJiaKui
 * @classname MqMsgDistributeHandle
 * @description TODO
 * @date 5/28/2021 10:23 AM
 */
public interface MqMsgDistributeHandle {

    MqMsgDistributeHandle getInstance();

    void distribute(MqMessage mqMessage);
}
