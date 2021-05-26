package com.zakl.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 代理客户端与代理服务器消息交换协议
 *
 * @author fengfei
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferMsg implements Serializable {

    /**
     * 心跳消息
     */

    public transient static final byte TYPE_HEARTBEAT = 0x07;



    /**
     * UDP Connect
     */
    public transient static final byte TYPE_UDP_CONNECT = 0x09;

    /**
     * 消息类型
     */
    private byte type;

    /**
     * 消息流水号(数据长度)
     */
    private long serialNumber;

    /**
     * 消息命令请求信息
     */
    private String uri;

    /**
     * 消息传输数据
     */
    private byte[] data;


    @Override
    public String toString() {
        return "ProxyMessage [type=" + type + ", serialNumber=" + serialNumber + ", uri=" + uri + ", data=" + Arrays.toString(data) + "]";
    }


}
