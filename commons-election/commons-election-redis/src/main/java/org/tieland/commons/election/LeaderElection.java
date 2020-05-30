package org.tieland.commons.election;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>master选举</p>
 * @author zhouxiang
 * @date 2020/5/30 10:09
 */
@Slf4j
public class LeaderElection {

    private static final String LOCK_PREFIX = "org:tieland:commons:election:";

    private RedissonClient redissonClient;

    private ElectionConfig config;

    private volatile boolean stopped = false;

    private Object initLock = new Object();

    private Object addLock = new Object();

    private volatile boolean master = false;

    private volatile boolean started = false;

    private List<ElectionListener> listeners = new ArrayList<>();

    private ElectionThread electionThread;

    public LeaderElection(RedissonClient redissonClient, ElectionConfig config){
        this.config = config;
        this.redissonClient = redissonClient;
    }

    /**
     * start
     */
    public void start(){
        String threadName = "ElectionThread-"+System.identityHashCode(this);
        electionThread = new ElectionThread(threadName);
        electionThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(()->stop()));
    }

    /**
     * stop
     */
    public void stop(){
        log.info(" ElectionThread is stopping ");

        if(stopped){
            return;
        }

        stopped = true;

        try {
            electionThread.join();
        } catch (InterruptedException e) {
            log.error("", e);
        }

        log.info(" ElectionThread is stopped ");
    }

    /**
     * 添加listener
     * @param listener
     */
    public void addElectionListener(ElectionListener listener){
        synchronized (addLock){
            if(listeners.contains(listener)){
                return;
            }

            listeners.add(listener);
        }
    }

    /**
     * 是否master
     * @return
     */
    public boolean isMaster(){
        synchronized (initLock){
            if(!started) {
                try {
                    log.info(" wait starting... ");
                    initLock.wait();
                } catch (InterruptedException e) {
                    log.error("", e);
                }
            }
        }

        return master;
    }

    /**
     * 选主Thread
     */
    private class ElectionThread extends Thread {

        ElectionThread(String name){
            setName(name);
            setDaemon(true);
        }

        @Override
        public void run() {
            if(config.getDelayStartSeconds()>0){
                try {
                    log.debug(" ElectionThread will start in {} seconds. ", config.getDelayStartSeconds());
                    TimeUnit.SECONDS.sleep(config.getDelayStartSeconds());
                } catch (InterruptedException e) {
                    log.error("", e);
                }
            }

            log.debug(" ElectionThread is starting ");

            RLock lock = redissonClient.getLock(LOCK_PREFIX+config.getName());

            while(!stopped){
                try{
                    //如果是master，检查是否仍然有效
                    if(master){
                        if(!(lock.isLocked() && lock.isHeldByCurrentThread())){
                            master = false;
                        }
                    }

                    //先去竞争获取lock
                    if(!master){
                        try {
                            master = lock.tryLock(config.getCheckIntervalSeconds(), TimeUnit.SECONDS);
                            if(master){
                                if(CollectionUtils.isNotEmpty(listeners)){
                                    listeners.stream().forEach(listener -> listener.onMaster());
                                }
                            }
                        } catch (InterruptedException e) {
                            log.error("", e);
                        }
                    }

                }catch (Exception ex){
                    master = false;
                    log.error("occur error", ex);
                }finally {
                    synchronized (initLock){
                        if(!started) {
                            started = true;
                            initLock.notifyAll();
                            log.debug(" ElectionThread is started ");
                        }
                    }
                }

                try {
                    Thread.sleep(TimeUnit.MILLISECONDS.convert(config.getCheckIntervalSeconds(), TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    log.error("", e);
                }
            }

            if(lock.isLocked() && lock.isHeldByCurrentThread()){
                lock.unlock();
            }

            if(master){
                master = false;
            }
        }
    }

}
