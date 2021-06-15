package com.zakl;

import com.zakl.consume.MqSubScribe;
import com.zakl.dto.MqMessage;
import com.zakl.msgdistribute.TemplateConsumer;

import java.util.List;

/**
 * @author ZhangJiaKui
 * @classname Consumer
 * @description TODO
 * @date 6/15/2021 9:37 AM
 */
public class Consumer extends TemplateConsumer {

    @Override
    @MqSubScribe(keys = "k1")
    public void consumeWithAutoAck(List<MqMessage> msgs) {
        for (MqMessage msg : msgs) {
            System.out.println(msg);
        }
        super.consumeWithAutoAck(msgs);
    }
}
