package com.zakl.protocol;

import cn.hutool.core.lang.Pair;
import io.protostuff.Morph;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MqPubMessage implements Serializable {

    /**
     * 心跳消息
     */
    public transient static final byte TYPE_HEARTBEAT = 0x00;

    /**
     * mq发布消息
     */
    public transient static final byte TYPE_PUBLISH = 0x01;


    /**
     * 消息类型
     */
    private byte type;

    /**
     * clientId
     */
    private String clientId;


    /**
     * mq推送数据
     * key->mq_key
     * value->{
     * value.key->score (-1 表示当前数据分发到 MQ_LIST:key)
     * value.key->score (>=0 表示当前数据分发到 MQ_SORTED_SET:key)
     * }
     */
    @Morph
    private Map<String, List<Pair<Double, String>>> pubMessages;


}
