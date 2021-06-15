package com.zakl.protocol;

import com.zakl.constant.Constants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IdleCheckHandler extends IdleStateHandler {


    public static final int READ_IDLE_TIME = 60;

    public static final int WRITE_IDLE_TIME = 40;


    private final Class<? extends SupMqMessage> mqMessageType;

    private final String clientId;


    public IdleCheckHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds, Class<? extends SupMqMessage> mqMsgType, String clientId) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
        this.mqMessageType = mqMsgType;
        this.clientId = clientId;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        if (IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT == evt) {
            log.info("channel write idle state {}", ctx.channel());
            SupMqMessage heartBeatMsg = this.mqMessageType.newInstance();
            heartBeatMsg.setType(Constants.TYPE_HEARTBEAT);
            heartBeatMsg.setClientId(clientId);
            ctx.channel().writeAndFlush(heartBeatMsg);
        } else if (IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT == evt) {
            log.warn("channel first read timeout {}", ctx.channel());
            ctx.channel().close();
        }
        super.channelIdle(ctx, evt);
    }
}
