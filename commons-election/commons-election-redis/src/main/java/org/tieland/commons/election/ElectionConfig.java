package org.tieland.commons.election;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zhouxiang
 * @date 2020/5/30 9:54
 */
@Data
@ConfigurationProperties(prefix = "config.common.election")
public class ElectionConfig {

    private static final String DEFAULT_LOCK_NAME = "leader";

    private String name = DEFAULT_LOCK_NAME;

    private long checkIntervalSeconds = 5;

    private long delayStartSeconds = 3;

}
