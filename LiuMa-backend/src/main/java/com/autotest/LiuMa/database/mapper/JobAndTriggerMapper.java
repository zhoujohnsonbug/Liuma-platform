package com.autotest.LiuMa.database.mapper;

import com.autotest.LiuMa.database.domain.JobAndTrigger;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by haoxy on 2018/9/28.
 * E-mail:hxyHelloWorld@163.com
 * github:https://github.com/haoxiaoyong1014
 */
@Repository
public interface JobAndTriggerMapper {

    List<JobAndTrigger> getJobAndTriggerDetails();
}
