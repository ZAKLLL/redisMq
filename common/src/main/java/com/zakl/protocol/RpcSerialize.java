
package com.zakl.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface RpcSerialize {

     <T> void serialize(OutputStream output, T object) throws IOException;

     <T> Object deserialize(InputStream input, Class<T> msgClass) throws IOException;
}

