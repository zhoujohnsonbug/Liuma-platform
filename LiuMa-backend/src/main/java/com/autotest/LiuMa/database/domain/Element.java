package com.autotest.LiuMa.database.domain;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class Element implements Serializable {
    private String id;

    private Long num;

    private String name;

    private String moduleId;

    private String projectId;

    private String by;

    private String expression;

    private String description;

    private Long createTime;

    private Long updateTime;

    private String createUser;

    private String updateUser;

    private String status;

}