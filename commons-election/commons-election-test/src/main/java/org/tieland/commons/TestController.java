package org.tieland.commons;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tieland.commons.election.LeaderElection;

/**
 * @author zhouxiang
 * @date 2020/5/30 14:46
 */
@RestController
public class TestController {

    @Autowired
    private LeaderElection leaderElection;

    @GetMapping("/stop")
    public String stop(){
        leaderElection.stop();
        return "success";
    }

}
