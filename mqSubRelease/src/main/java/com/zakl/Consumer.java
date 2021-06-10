package com.zakl;


import com.zakl.consume.MqSubScribe;
import com.zakl.dto.MqMessage;
import com.zakl.msgdistribute.TemplateConsumer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Zakl
 * @version 1.0
 * @className ComSumer
 * @date 6/9/2021
 * @desc todo
 **/
@Slf4j
public class Consumer extends TemplateConsumer {
    @MqSubScribe(keys = {"k1", "k3"})
    @Override
    public void consumeWithAutoAck(List<MqMessage> msgs) {
        for (MqMessage msg : msgs) {
            log.info(msg.toString());
        }
        super.consumeWithAutoAck(msgs);
    }
}
