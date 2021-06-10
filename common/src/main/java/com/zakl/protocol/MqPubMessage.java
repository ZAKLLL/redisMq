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
