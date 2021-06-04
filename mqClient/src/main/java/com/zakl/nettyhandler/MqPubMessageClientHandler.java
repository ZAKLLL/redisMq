package com.zakl.nettyhandler;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.UUID;
import com.zakl.constant.Constants;
import com.zakl.protocol.MqPubMessage;
import com.zakl.protocol.MqSubMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author ZhangJiaKui
 * @classname MqSubMessageHandler
 * @description TODO
 * @date 5/27/2021 4:26 PM
 */
@Slf4j
public class MqPubMessageClientHandler extends SimpleChannelInboundHandler<MqSubMessage> {

    private ChannelHandlerContext ctx;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqSubMessage msg) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("【" + ctx.channel().id() + "】" + new SimpleDateFormat("yyyy/MM/dd HH/mm/ss").format(new Date()) + "==>>>"
                + "channelActive");


        this.ctx = ctx;
        Random random = new Random();

        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                while (true) {
                    String[] keys = {"k1", "k2", "k3", "k4"};
                    for (String key : keys) {
                        String value = "HelloWorld" + System.currentTimeMillis();
                        MqPubMessage mqPubMessage = new MqPubMessage();
                        mqPubMessage.setClientId(UUID.randomUUID().toString());
                        Map<String, List<Pair<Double, String>>> map = new HashMap<>();

                        map.put(key, ListUtil.toList(new Pair<>(random.nextDouble() * 100, value)));
                        mqPubMessage.setPubMessages(map);
                        ctx.writeAndFlush(mqPubMessage);
                        Thread.sleep(1000);
                    }
                    Thread.sleep(5000);
                }
            }
        }).start();
        super.channelActive(ctx);
    }
}
