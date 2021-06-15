package com.zakl.protocol;

import cn.hutool.core.lang.Pair;
import io.protostuff.Morph;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class MqPubMessage extends SupMqMessage implements Serializable {


    /**
     * mq发布消息
     */
    public transient static final byte TYPE_PUBLISH = 0x01;

    /**
     * ack
     */
    public transient static final byte TYPE_ACK = 0x04;

    @Morph
    private Map<String, List<Pair<Double, String>>> pubMessages;

    /**
     * push ack id,ensure mqServer receive  pubMessages;
     */
    private String pubId;

}
