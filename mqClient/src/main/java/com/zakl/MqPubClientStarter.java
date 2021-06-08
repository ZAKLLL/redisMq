package com.zakl;

import cn.hutool.core.lang.Pair;
import com.zakl.dto.MqMessage;
import com.zakl.nettyhandle.MqClientContainer;
import com.zakl.passivecall.PassiveCaller;
import lombok.SneakyThrows;

import java.util.List;

/**
 * @author ZhangJiaKui
 * @classname MqPubClientStarter
 * @description TODO
 * @date 6/4/2021 4:31 PM
 */
public class MqPubClientStarter {
    public static void main(String[] args) {
        new MqClientContainer(true).start();
    }
}

