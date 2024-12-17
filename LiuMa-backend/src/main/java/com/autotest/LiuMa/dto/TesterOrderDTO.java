package com.autotest.LiuMa.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class TesterOrderDTO {

    private Long id;
    private String ranker;

    private String runTime;

    private String frequency;

    private String status;

    private String deleted;

    private String jobId;
}
