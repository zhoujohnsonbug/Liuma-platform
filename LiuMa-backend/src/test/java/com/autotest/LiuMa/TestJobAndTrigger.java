package com.autotest.LiuMa;


import com.autotest.LiuMa.database.domain.JobAndTrigger;
import com.autotest.LiuMa.database.domain.TesterOrder;
import com.autotest.LiuMa.database.mapper.JobAndTriggerMapper;
import com.autotest.LiuMa.database.mapper.TesterOrderMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestJobAndTrigger {
    @Autowired
    private JobAndTriggerMapper jobAndTriggerMapper;
    @Autowired
    private TesterOrderMapper testerOrderMapper;


    @Test
    public void TestGet() {
        List<JobAndTrigger> jobAndTriggerDetails = jobAndTriggerMapper.getJobAndTriggerDetails();
        System.out.println(jobAndTriggerDetails);
    }

    @Test
    public void TestByJobId() {
        TesterOrder testerOrderByJobId = testerOrderMapper.getTesterOrderByJobId("4d4c9788-b64b-40ec-9b02-7588649ab2b6");
        System.out.println(testerOrderByJobId);
    }


}
