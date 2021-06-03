package com.zakl.statusManage;

import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class SubClientInfo {
    private final String clientId;
    private final Integer weight;
    private final ChannelHandlerContext context;
    public volatile boolean isAlive = true;

    public SubClientInfo(String clientId, Integer weight, ChannelHandlerContext context) {
        this.clientId = clientId;
        this.weight = weight;
        this.context = context;
    }
}
