import com.zakl.container.ContainerHelper;
import com.zakl.container.MqServerContainer;
import com.zakl.protocol.MqPubMessage;
import com.zakl.protocol.MqSubMessage;
import com.zakl.statusManage.StatusManager;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.lang.management.ManagementFactory;

/**
 * @author ZhangJiaKui
 * @classname Main
 * @description TODO
 * @date 5/25/2021 2:13 PM
 */
@Slf4j
public class RedisMqStarter {


    private final static NioEventLoopGroup serverBossGroup = new NioEventLoopGroup();

    public static void initServerData() {
        StatusManager.initFromRedis();
    }


    public static void main(String[] args) {
        MDC.put("process_id ", ManagementFactory.getRuntimeMXBean().getName());
        initServerData();

        MqServerContainer mqServerContainer = new MqServerContainer(serverBossGroup, MqPubMessage.class);
        MqServerContainer mqSubServerContainer = new MqServerContainer(serverBossGroup, MqSubMessage.class);
        ContainerHelper.start(mqServerContainer, mqSubServerContainer);
    }
}
