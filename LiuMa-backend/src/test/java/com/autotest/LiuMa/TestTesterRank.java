package com.autotest.LiuMa;

import com.autotest.LiuMa.database.domain.TesterOrder;
import com.autotest.LiuMa.database.domain.TesterRank;
import com.autotest.LiuMa.database.mapper.TesterOrderMapper;
import com.autotest.LiuMa.database.mapper.TesterRankMapper;
import com.autotest.LiuMa.service.ScheduleJobService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestTesterRank {

    @Autowired
    TesterRankMapper testerRankMapper;

    @Autowired
    ScheduleJobService scheduleJobService;

    @Autowired
    TesterOrderMapper testerOrderMapper;

    @Test
    public void getBackUpFile() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            List<TesterRank> thur = testerRankMapper.getTesterRankByCode("thur");
//        System.out.println(thur);
            if (thur.size() > 0) {
                TesterRank testerRank = thur.get(0);
                String name = testerRank.getName();
                System.out.println(name);
                testerRankMapper.updateTesterRank(testerRank);
            } else {
                TesterRank testerRank = new TesterRank();
                testerRank.setCode("thur");
                testerRankMapper.updateTesterRankUnfinished(testerRank);
                List<TesterRank> thur1 = testerRankMapper.getTesterRankByCode("thur");
                TesterRank testerRank1 = thur1.get(0);
                String name = testerRank1.getName();
                System.out.println(name);
                testerRankMapper.updateTesterRank(testerRank1);
            }
            Thread.currentThread().sleep(1000);
        }

    }

    @Test
    public void getData() throws InterruptedException {
        scheduleJobService.AutoRemindRequireProcedureNew();
    }

    @Test
    public void gethello() {
        TesterOrder testerOrderById = testerOrderMapper.getTesterOrderById(1L);
        System.out.println(testerOrderById);
        String frequency = testerOrderById.getFrequency();
        String runTime = testerOrderById.getRunTime();
        System.out.println(Long.parseLong(runTime)*1000);
        Date date = new Date(Long.parseLong(runTime)*1000);
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
        System.out.println(s.substring(0, s.length() - 1));

    }

    public  String CronString(Long id) {
        TesterOrder testerOrderById = testerOrderMapper.getTesterOrderById(id);
//        System.out.println(testerOrderById);
        String frequency = testerOrderById.getFrequency();
        String runTime = testerOrderById.getRunTime();
        System.out.println(Long.parseLong(runTime)*1000);
        Date date = new Date(Long.parseLong(runTime)*1000);
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
        return s.substring(0, s.length() - 1);
    }

    @Test
    public void gethello1() {
        String s = CronString(1L);
        System.out.println(s);
    }

    @Test
    public void testgetAll() {
        List<TesterOrder> allTesterOrder = testerOrderMapper.getAllTesterOrder();
        System.out.println(allTesterOrder);
    }
}
