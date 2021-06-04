package com.zakl.protostuff;


import com.zakl.protocol.RpcSerialize;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings("all")
public class ProtostuffSerialize implements RpcSerialize {

    @Override
    public <T> Object deserialize(InputStream input, Class<T> msgClass) throws IOException {
        Schema<T> schema = RuntimeSchema.getSchema(msgClass);
        try {
            Object message = msgClass.newInstance();
            ProtostuffIOUtil.mergeFrom(input, ((T) message), schema);
            return message;
        } catch (InstantiationException | IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void serialize(OutputStream output, T object) {
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(object.getClass());
            ProtostuffIOUtil.writeTo(output, object, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

}

