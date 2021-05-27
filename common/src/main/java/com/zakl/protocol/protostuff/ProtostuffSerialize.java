package com.zakl.protocol.protostuff;


import com.zakl.protocol.RpcSerialize;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProtostuffSerialize implements RpcSerialize {


    @Override
    public Object deserialize(InputStream input, Class msgClass) {
        Schema schema = RuntimeSchema.getSchema(msgClass);
        try {
            Object message = msgClass.newInstance();
            ProtostuffIOUtil.mergeFrom(input, message, schema);
            return message;
        } catch (InstantiationException | IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void serialize(OutputStream output, Object object) {
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema schema = RuntimeSchema.getSchema(object.getClass());
            ProtostuffIOUtil.writeTo(output, object, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }
}

