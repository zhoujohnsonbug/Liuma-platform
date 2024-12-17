package com.autotest.LiuMa;

import com.autotest.LiuMa.common.utils.TapdUtils;
import com.autotest.LiuMa.database.domain.StorySatistics;
import com.autotest.LiuMa.database.domain.TesterOrder;
import com.autotest.LiuMa.database.domain.TesterRank;
import com.autotest.LiuMa.database.mapper.StorySatisticsMapper;
import com.autotest.LiuMa.database.mapper.TesterOrderMapper;
import com.autotest.LiuMa.database.mapper.TesterRankMapper;
import com.autotest.LiuMa.service.ScheduleJobService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestTesterOrder {


    @Autowired
    StorySatisticsMapper storySatisticsMapper;

    @Test
    public void getBackUpFile() throws InterruptedException {
        //查询出最新的需求内容
        List<StorySatistics> allStorySatistics1 = storySatisticsMapper.getAllStorySatistics();

        Collections.sort(allStorySatistics1, new Comparator<StorySatistics>() {
            @Override
            public int compare(StorySatistics o1, StorySatistics o2) {
                return Integer.parseInt(o1.getStatusSort())-Integer.parseInt(o2.getStatusSort());
            }
        });
        Map<String, Integer> map = TapdUtils.countOccurrence(allStorySatistics1);

        StringBuilder sbAll = new StringBuilder();

        sbAll.append("@所有人，本周需发布的需求，截止今天的测试情况：总共"+allStorySatistics1.size()+"个需求,其中：");
        System.out.println(map);
        for (Map.Entry<String, Integer> entry : map.entrySet()) {

            if ("1".equals(entry.getKey())) {
                sbAll.append("预发布集成测试通过 " + map.get(entry.getKey()) + " 个 ");
            }else if ("2".equals(entry.getKey())) {
                sbAll.append("预发布集成测试 " + map.get(entry.getKey()) + " 个 ");
            }else if ("3".equals(entry.getKey())) {
                sbAll.append("集成测试通过 " + map.get(entry.getKey()) + " 个 ");
            }else if ("4".equals(entry.getKey())) {
                sbAll.append("集成测试 " + map.get(entry.getKey()) + " 个 ");
            }else if ("5".equals(entry.getKey())) {
                sbAll.append("测试通过 " + map.get(entry.getKey()) + " 个 ");
            }else if ("6".equals(entry.getKey())) {
                sbAll.append("测试中 " + map.get(entry.getKey()) + " 个 ");
            }

        }
        sbAll.append("\n");
        boolean added_status_11 = false;
        boolean added_status_10 = false;
        boolean added_status_9 = false;
        boolean added_status_8 = false;
        boolean added_status_5 = false;
        boolean added_testing = false;
//        sbAll.append("[太阳] 预发布集成测试通过： \n");
        for (int i = 0; i < allStorySatistics1.size(); i++) {
            StorySatistics storySatistics = allStorySatistics1.get(i);
            String status = storySatistics.getStatus();
            if ("status_11".equals(status)) {
                if (!added_status_11) {
                    sbAll.append("[太阳] 预发布集成测试通过： \n");
                    added_status_11 = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            } else if ("status_10".equals(status)) {
                if (!added_status_10) {
                    sbAll.append("[太阳] 预发布集成测试： \n");
                    added_status_10 = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            } else if ("status_9".equals(status)) {
                if (!added_status_9) {
                    sbAll.append("[太阳] 集成测试通过： \n");
                    added_status_9 = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            }else if ("status_8".equals(status)) {
                if (!added_status_8) {
                    sbAll.append("[太阳] 集成测试： \n");
                    added_status_8 = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            }else if ("status_3".equals(status)||"status_4".equals(status)||"status_5".equals(status)) {
                if (!added_status_5) {
                    sbAll.append("[太阳] 测试通过： \n");
                    added_status_5 = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            }else if ("testing".equals(status)) {
                if (!added_testing) {
                    sbAll.append("[太阳] 产品测试中： \n");
                    added_testing = true;
                }
                sbAll.append(i+1 +"、" + storySatistics.getStoryName()+ "\n");
            }
        }

        String s1 = sbAll.toString();
        System.out.println(s1);

    }


}
