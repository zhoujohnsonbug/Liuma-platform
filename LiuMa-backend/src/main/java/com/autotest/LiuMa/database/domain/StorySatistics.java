package com.autotest.LiuMa.database.domain;

import lombok.Data;

@Data
public class StorySatistics {
    private String id;
    private String storyId;
    private String shortId;
    private String storyName;
    private String statusAlias;
    private String status;

    private String statusSort;
    private String owner;
    private String releasePlan;
    private String storyUrl;
    private String deleted;
    private String workspaceId;
    private Long createTime;

    private Long updateTime;
}
