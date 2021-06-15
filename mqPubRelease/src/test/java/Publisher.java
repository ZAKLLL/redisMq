import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import com.zakl.MqServiceLoader;
import com.zakl.config.InitClientService;
import lombok.SneakyThrows;

import java.util.Date;
import java.util.Random;

/**
 * @author ZhangJiaKui
 * @classname Publisher
 * @description TODO
 * @date 6/15/2021 9:22 AM
 */
public class Publisher {



    public static void main(String[] args) {

        InitClientService.init();

        Random random = new Random();

        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                Thread.sleep(2000);
                while (true) {
                    Thread.sleep(1000);
                    String[] keys = {"k9"};
                    for (String key : keys) {
                        String value = "HelloWorld" + DateUtil.format(new Date(), "yyyy/MM/dd HH:mm:ss");
                        boolean b = com.zakl.publish.Publisher.publishToSortedSetKey(key, new Pair<>(random.nextDouble() * 100, value));
                        System.out.println("发送---->" + b);
                    }
                }
            }
        }).start();
    }
}
