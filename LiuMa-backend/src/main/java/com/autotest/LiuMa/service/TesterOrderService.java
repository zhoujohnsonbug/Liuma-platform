package com.autotest.LiuMa.service;


import com.autotest.LiuMa.common.utils.SpringUtil;
import com.autotest.LiuMa.database.domain.JobInfo;
import com.autotest.LiuMa.database.domain.TesterOrder;
import com.autotest.LiuMa.database.mapper.TesterOrderMapper;
import com.autotest.LiuMa.service.QuartzJob.BaseJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class TesterOrderService {

//    @Value("${test.file.path}")
//    public String TEST_FILE_PATH;
//
//    @Value("${app.package.path}")
//    public String APP_PACKAGE_PATH;

    @Resource
    private TesterOrderMapper testerOrderMapper;

    //加入Qulifier注解，通过名称注入bean
    @Autowired
    @Qualifier("Scheduler")
    private Scheduler scheduler;


    //修改和逻辑删除数据
    public void updateTesterOrder(TesterOrder testerOrder) throws Exception {
        if (testerOrder.getId() != null || !"".equals(testerOrder.getId())) {
            if (!"".equals(testerOrder.getRunTime())) {

                String jobClassName = "wjxNotificationJob";
                String jobGroupName = testerOrder.getJobId();
                String frequency = testerOrder.getFrequency();
                String runTime = testerOrder.getRunTime();
                String cronExpression = CronString(frequency, runTime);
                jobreschedule(jobClassName,jobGroupName,cronExpression);
                if ("0".equals(testerOrder.getStatus())) {
                    jobPause(jobClassName,jobGroupName);
                }
            }
            testerOrderMapper.updateTesterOrder(testerOrder);

        }
    }

    //查所有数据
    public List<TesterOrder> getAllTesterOrder() {
        return testerOrderMapper.getAllTesterOrder();
    }

    //插入数据
    public void createTesterOrder(TesterOrder testerOrder) throws Exception {
        String frequency = testerOrder.getFrequency();
        String runTime = testerOrder.getRunTime();
        String s = CronString(frequency, runTime);

        JobInfo jobInfo = new JobInfo();
        jobInfo.setCronExpression(s);
        jobInfo.setJobClassName("wjxNotificationJob");
        String jobId = String.valueOf(UUID.randomUUID());
        jobInfo.setJobGroupName(jobId);
        addCronJob(jobInfo);
        testerOrder.setJobId(jobId);
        testerOrderMapper.addTesterOrder(testerOrder);
    }

    //逻辑删除数据
    public void deleteTesterOrder(Long id) throws Exception {
        TesterOrder testerOrder = new TesterOrder();
        testerOrder.setId(id);
        testerOrder.setDeleted("1");
        testerOrder.setStatus("0");
        TesterOrder testerOrderById = testerOrderMapper.getTesterOrderById(id);
        String jobId = testerOrderById.getJobId();
        String  jobClassName= "wjxNotificationJob";
        jobdelete(jobClassName,jobId);
        testerOrderMapper.updateTesterOrder(testerOrder);
    }

    public void control(TesterOrder testerOrder) throws Exception {
//        1、修改testerorder库状态
//        2、变更定时任务执行状态（如果是第一次启动则 start ，第一次暂停则 pause，）
//        3 如果状态为0，则暂停，如果状态为1 则 start或者 resume
//        4、当删除一条数据的时候，这条定时任务也要删除
//        5、当任务发生修改时，需要暂停任务（或者删除旧任务，创建一个新任务）
        String jobId = testerOrder.getJobId();
        String  jobClassName= "wjxNotificationJob";
        updateTesterOrder(testerOrder);
        if ("0".equals(testerOrder.getStatus())) {
            jobPause(jobClassName,jobId);
        }
        if ("1".equals(testerOrder.getStatus())) {
            jobresume(jobClassName,jobId);
        }



    }


    public  String CronString(String frequency,String runTime) {
//        TesterOrder testerOrderById = testerOrderMapper.getTesterOrderById(id);
////        System.out.println(testerOrderById);
//        String frequency = testerOrderById.getFrequency();
//        String runTime = testerOrderById.getRunTime();
//        System.out.println(Long.parseLong(runTime)*1000);
        Long runTimeLong= Long.parseLong(runTime)*1000;
        Date date = new Date(runTimeLong);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        String format = simpleDateFormat.format(date);
        String[] timeList = format.split("\\:");
        String[] frequencyList = frequency.split("\\|");
        StringBuilder cronStr = new StringBuilder();
//        0 30 8 ? * FRI
        cronStr.append("0 ").append(timeList[1]+" "+timeList[0]+" ? * ");
        for (int i = 0; i < frequencyList.length; i++) {
            if ("1".equals(frequencyList[i])) {
                cronStr.append("MON,");
            }
            if ("2".equals(frequencyList[i])) {
                cronStr.append("TUE,");
            }
            if ("3".equals(frequencyList[i])) {
                cronStr.append("WED,");
            }
            if ("4".equals(frequencyList[i])) {
                cronStr.append("THU,");
            }
            if ("5".equals(frequencyList[i])) {
                cronStr.append("FRI,");
            }
            if ("6".equals(frequencyList[i])) {
                cronStr.append("SAT,");
            }
            if ("7".equals(frequencyList[i])) {
                cronStr.append("SUN");
            }
        }
        String s = cronStr.toString();
        return s;
    }

    //CronTrigger
    public void addCronJob(JobInfo jobInfo) throws Exception {

        // 启动调度器
        scheduler.start();

        //构建job信息
        JobDetail jobDetail = JobBuilder.newJob(getClass(jobInfo.getJobClassName()).getClass()).
                withIdentity(jobInfo.getJobClassName(), jobInfo.getJobGroupName())
                .build();

        //表达式调度构建器(即任务执行的时间)
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(jobInfo.getCronExpression());
        //按新的cronExpression表达式构建一个新的trigger
        CronTrigger trigger = TriggerBuilder.newTrigger().
                withIdentity(jobInfo.getJobClassName(), jobInfo.getJobGroupName())
                .withSchedule(scheduleBuilder)
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);

        } catch (SchedulerException e) {
            System.out.println("创建定时任务失败" + e);
            throw new Exception("创建定时任务失败");
        }
    }

    public BaseJob getClass(String classname) throws Exception {
        //Class<?> class1 = Class.forName(classname);
        //BaseJob baseJob = (BaseJob) class1.newInstance();
        BaseJob baseJob = (BaseJob) SpringUtil.getBean(classname);
        return baseJob;
    }

    public void jobreschedule(String jobClassName, String jobGroupName, String cronExpression) throws Exception {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobClassName, jobGroupName);
            // 表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);

            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

            // 按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

            // 按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (SchedulerException e) {
            System.out.println("更新定时任务失败" + e);
            throw new Exception("更新定时任务失败");
        }
    }

    public void jobdelete(String jobClassName, String jobGroupName) throws Exception {
        scheduler.pauseTrigger(TriggerKey.triggerKey(jobClassName, jobGroupName));
        scheduler.unscheduleJob(TriggerKey.triggerKey(jobClassName, jobGroupName));
        scheduler.deleteJob(JobKey.jobKey(jobClassName, jobGroupName));
    }

    //更新任務
    public void jobresume(String jobClassName, String jobGroupName) throws Exception {
        scheduler.resumeJob(JobKey.jobKey(jobClassName, jobGroupName));
    }
    //暫停任務
    public void jobPause(String jobClassName, String jobGroupName) throws Exception {
        scheduler.pauseJob(JobKey.jobKey(jobClassName, jobGroupName));
    }

}
