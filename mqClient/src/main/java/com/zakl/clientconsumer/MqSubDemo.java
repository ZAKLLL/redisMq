package com.zakl.clientconsumer;

import com.zakl.dto.MqMessage;
import com.zakl.msgdistribute.TemplateConsumer;
import com.zakl.msgdistribute.AckClientHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MqSubDemo extends TemplateConsumer {


//    @MqSubScribe(keys = {"k1"})
    @Override
    public void consumeWithAutoAck(List<MqMessage> msgs) {
        for (MqMessage msg : msgs) {
            log.info(msg.toString());
        }
    }

//    @MqSubScribe(keys = {"k3"}, autoAck = false)
    @Override
    public void consumeWithManualAck(List<MqMessage> msgs, AckClientHandler ackHandler) {
        super.consumeWithManualAck(msgs, ackHandler);
        ackHandler.confirm(msgs.toArray(new MqMessage[0]));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
