package com.autotest.LiuMa.database.domain;

import lombok.Data;
import lombok.ToString;

import java.math.BigInteger;

/**
 * Created by haoxy on 2018/9/28.
 * E-mail:hxyHelloWorld@163.com
 * github:https://github.com/haoxiaoyong1014
 * quartz 专用
 */

@Data
@ToString
public class JobAndTrigger {

    private String jobName;
    private String jobGroup;
    private String jobClassName;
    private String triggerName;
    private String triggerGroup;
    private BigInteger  repeatInterval;
    private BigInteger  timesTriggered;
    private String  cronExpression;
    private String  timeZoneId;

}
