package org.tieland.commons.election;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouxiang
 * @date 2020/5/30 9:52
 */
@Configuration
@EnableConfigurationProperties(ElectionConfig.class)
public class RedisLeaderElectionAutoConfiguration {

    @Bean(
            initMethod = "start",
            destroyMethod = "stop"
    )
    @ConditionalOnMissingBean(LeaderElection.class)
    public LeaderElection leaderElection(RedissonClient redissonClient, ElectionConfig config){
        return new LeaderElection(redissonClient, config);
    }

}
