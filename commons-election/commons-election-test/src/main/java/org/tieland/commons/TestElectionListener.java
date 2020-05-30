package org.tieland.commons;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tieland.commons.election.ElectionListener;
import org.tieland.commons.election.LeaderElection;

import javax.annotation.PostConstruct;

/**
 * @author zhouxiang
 * @date 2020/5/30 12:26
 */
@Slf4j
@Component
public class TestElectionListener implements ElectionListener {

    @Autowired
    private LeaderElection leaderElection;

    public TestElectionListener(){

    }

    @PostConstruct
    public void init(){
        leaderElection.addElectionListener(this);
    }

    @Override
    public void onMaster() {
        log.info(" do master ");
    }
}
