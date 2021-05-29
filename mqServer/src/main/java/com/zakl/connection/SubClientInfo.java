package com.zakl.connection;

import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class SubClientInfo {
    private String clientId;
    private Integer weight;
    private ChannelHandlerContext context;
}
