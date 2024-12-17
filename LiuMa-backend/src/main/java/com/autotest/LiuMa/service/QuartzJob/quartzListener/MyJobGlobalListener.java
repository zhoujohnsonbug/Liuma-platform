package com.autotest.LiuMa.service.QuartzJob.quartzListener;

import com.autotest.LiuMa.common.utils.NameOrderUtils;
import com.autotest.LiuMa.common.utils.SpringUtil;
import com.autotest.LiuMa.common.utils.StringUtils;
import com.autotest.LiuMa.database.domain.TesterOrder;
import com.autotest.LiuMa.database.mapper.TesterOrderMapper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MyJobGlobalListener implements JobListener {

//    @Autowired
//    private TesterOrderMapper testerOrderMapper;

    @Override
    public String getName() {
        return "MyJobGlobalListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
//        String jobName = context.getJobDetail().getKey().getName();
//        String group = context.getJobDetail().getKey().getGroup();
//        System.out.println("Job " + jobName+"-->"+group + " is about to be executed.");
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        String jobName = context.getJobDetail().getKey().getName();
        String group = context.getJobDetail().getKey().getGroup();
        System.out.println("Job " + jobName+"-->"+group  + " was vetoed and not executed.");
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException e) {
        String jobName = context.getJobDetail().getKey().getName();
        String group = context.getJobDetail().getKey().getGroup();
        if (e == null) {
            //1、根据获取的 jobname 和 group来判断任务是否存在
//            System.out.println("group:"+group);
            // 使用 MyBatis 查询用户信息
            //通过反射方式获取mapper
            TesterOrderMapper testerOrderMapper = SpringUtil.getBean(TesterOrderMapper.class);

            TesterOrder testerOrderByJobId = testerOrderMapper.getTesterOrderByJobId(group);
            String ranker = testerOrderByJobId.getRanker();
            String s = NameOrderUtils.convertString(ranker);
            testerOrderByJobId.setRanker(s);
//            System.out.println(testerOrderByJobId);
            testerOrderMapper.updateTesterOrder(testerOrderByJobId);
            //2、如果存在则执行 姓名顺序切换，第一个排到最后一个
            System.out.println("Job " + jobName+"-->"+group  + " was executed successfully.");
        } else {
            System.out.println("Job " + jobName+"-->"+group  + " was executed with exception: " + e.getMessage());
        }
    }
}