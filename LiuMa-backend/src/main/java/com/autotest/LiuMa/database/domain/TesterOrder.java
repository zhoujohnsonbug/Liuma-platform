package com.autotest.LiuMa.database.domain;

import lombok.Data;

import java.util.List;

@Data
public class TesterOrder {
    private Long id;
    private String ranker;

    private String runTime;

    private String frequency;

    private String status;

    private String deleted;

    private String jobId;



}
