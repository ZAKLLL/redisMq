package com.zakl.container;

import com.zakl.annotation.MqSubScribe;
import com.zakl.nettyhandler.AckHandler;
import com.zakl.dto.MqMessage;

import java.util.List;

public class MqSubDemo {

    @MqSubScribe(keys = {"k1", "k2"})
    public void consume(final AckHandler ackHandler, List<MqMessage> msgs) {
        //do ack confirm
        msgs.forEach(ackHandler::confirm);
    }
}
