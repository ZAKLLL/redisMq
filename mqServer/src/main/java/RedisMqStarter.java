import com.zakl.container.ContainerHelper;
import com.zakl.container.MqServerContainer;
import com.zakl.protocol.MqPubMessage;
import com.zakl.protocol.MqSubMessage;
import com.zakl.statusManage.StatusManager;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

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
        //todo 将持久化的Redis信息 更新到到服务中
        StatusManager.initFromRedis();
    }


    public static void main(String[] args) {

        initServerData();

        MqServerContainer mqServerContainer = new MqServerContainer(serverBossGroup, MqPubMessage.class);
        MqServerContainer mqSubServerContainer = new MqServerContainer(serverBossGroup, MqSubMessage.class);
        ContainerHelper.start(mqServerContainer, mqSubServerContainer);
    }
}
