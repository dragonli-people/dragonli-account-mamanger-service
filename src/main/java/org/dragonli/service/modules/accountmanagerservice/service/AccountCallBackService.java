package org.dragonli.service.modules.accountmanagerservice.service;

import org.dragonli.service.modules.accountmanagerservice.executor.BusinessCallBackExecutor;
import org.dragonli.service.modules.accountservice.constants.AccountConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AccountCallBackService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${crypto.ui.mq-consumer.thread-num:20}")
    int threadNum;
    private final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Autowired
    @Qualifier(AccountConstants.ACCOUNT_REDIS)
    JedisPool jedisPool;

    @Value("${spring.dubbo-jackpot-service.info.redis.chainxServerPauseSignal:abc}")
    String serverPauseSignalRedisKey;

    @Value("${ACCOUNT_CALL_BACK_REDIS_KEY:" + AccountConstants.DEFAULT_ACCOUNT_CALL_BACK_REDIS_KEY + "}")
    String accountCallBackRedisKey;

    @Autowired
    BusinessCallBackExecutor callBackExecutor;

    @Value("${PRIMARY_ACCOUNT_CALL_BACK_HANDLER:false}")
    boolean isPrimary;

    static final ConcurrentMap<String,Boolean> hadHandles = new ConcurrentHashMap<>();

    @PostConstruct
    public void start() {
        if(!isPrimary)return;
        logger.info("启动tick线程读取redis数据");
//        for(int i = 0;i < threadNum; i++) {
            cachedThreadPool.execute(()-> {
                    // TODO Auto-generated method stub
                    try {
                        tick();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        logger.info("tick is error "+e);
                        //							e.printStackTrace();
                    }
                });
//        }
    }

    public void tick() throws InterruptedException {

        while(true){
//            Object serverSignal = jedisPool.getBucket(accountCallBackRedisKey).get();
//            if(serverSignal != null && !"".equals(serverSignal.toString().trim())
//                    && Integer.parseInt(serverSignal.toString()) >= 3 )
//            {
//                Thread.sleep(512L);
//                continue;
//            }
//            Thread.sleep(512L);
            Thread.sleep(32L);
            Jedis jedis = null;
            List<String> queue = null;
            try {
                jedis = jedisPool.getResource();
                //注意此处需要向左边push
                int len = jedis.llen(accountCallBackRedisKey).intValue();
                len = Math.min(len,100);
                if(len == 0)  continue;
                queue = jedis.lrange(accountCallBackRedisKey,0,len-1);
            }catch (Exception e){
                logger.error(e.getMessage(), e);
            }
            finally {
                if(jedis != null) jedis.close();
                jedis = null;
            }
//            RQueue<String> queue = jedisPool.getQueue(accountCallBackRedisKey);
            final JedisPool jedisPool1 = jedisPool;
            for( String content : queue ){
                if( hadHandles.containsKey(content) )continue;
                hadHandles.put(content,true);
                final String content1 = content;
                cachedThreadPool.execute(()->{
                    Jedis jedis1 = null;
                    try {
                        callBackExecutor.receiveRedisInfo(content1);
                        jedis1 = jedisPool1.getResource();
                        //处理完了（或处理过而被拒），无论如何应该状态处于正常，将其删除
                        jedis1.lrem(accountCallBackRedisKey,0,content1);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    finally {
                        if(jedis1 != null )jedis1.close();
                    }
                });
            }



        }

    }
}
