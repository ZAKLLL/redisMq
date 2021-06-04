package com.zakl.mqhandler;

import com.zakl.dto.MqMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author ZhangJiaKui
 * @classname TemplateConsumer
 * @description TODO
 * @date 6/4/2021 4:16 PM
 */
@Slf4j
public abstract class TemplateConsumer {


    public void consumeWithAutoAck(List<MqMessage> msgs) {
        for (MqMessage msg : msgs) {
            log.info("templateConsumer' consumeWithAutoAck receive:{}", msg);
        }
    }

    public void consumeWithManualAck(List<MqMessage> msgs, final AckClientHandler ackHandler) {
        for (MqMessage msg : msgs) {
            log.info("templateConsumer' consumeWithManualAck receive:{}", msg);
        }
    }


}
