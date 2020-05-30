package org.tieland.commons.election;

/**
 * <p>选举Listener</p>
 * <p>当被选举为master时回调</p>
 * @author zhouxiang
 * @date 2020/5/30 10:00
 */
public interface ElectionListener {

    /**
     * 当选为master
     */
    void onMaster();

}
