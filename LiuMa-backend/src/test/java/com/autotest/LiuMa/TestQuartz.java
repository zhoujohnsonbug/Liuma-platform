package com.autotest.LiuMa;



import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

//class HelloWorldJob implements Job {
//    public void execute(JobExecutionContext context) throws JobExecutionException {
//        System.out.println("Hello, World!");
//    }
//
//}
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestQuartz {

//    @Autowired
//    private QuartzScheduler quartzScheduler;

    @Test
    public void scheduleMyJob() throws Exception {
//        quartzScheduler.scheduleJob("0 0/1 * * * ?", HelloWorldJob.class);
        System.out.println("hello world");
    }
}
