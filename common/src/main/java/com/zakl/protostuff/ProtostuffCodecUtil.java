package com.zakl.protostuff;

import com.zakl.protocol.MessageCodecUtil;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


@SuppressWarnings("all")
public class ProtostuffCodecUtil implements MessageCodecUtil {
    private ProtostuffSerializePool pool = ProtostuffSerializePool.getProtostuffPoolInstance();

    private Class msgClass;

    public ProtostuffCodecUtil(Class msgClass) {
        this.msgClass = msgClass;
    }

    @Override
    public void encode(final ByteBuf out, final Object message) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            ProtostuffSerialize protostuffSerialization = pool.borrow();
            protostuffSerialization.serialize(byteArrayOutputStream, message);
            byte[] body = byteArrayOutputStream.toByteArray();
            int dataLength = body.length;
            out.writeInt(dataLength);
            out.writeBytes(body);
            pool.restore(protostuffSerialization);
        }
    }

    @Override
    public Object decode(byte[] body) throws IOException {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);) {
            ProtostuffSerialize protostuffSerialization = pool.borrow();
            Object obj = protostuffSerialization.deserialize(byteArrayInputStream, msgClass);
            pool.restore(protostuffSerialization);
            return obj;
        }
    }
}

