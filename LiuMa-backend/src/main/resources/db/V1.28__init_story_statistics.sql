-- liuma.story_statistics definition

CREATE TABLE `story_statistics` (
                                    `id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
                                    `story_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '需求id',
                                    `short_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '短id',
                                    `story_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '需求名',
                                    `status_alias` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '状态名称',
                                    `status` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '状态标识',
                                    `owner` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '处理人',
                                    `release_plan` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '发布计划',
                                    `story_url` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'tapd链接',
                                    `create_time` bigint DEFAULT NULL COMMENT '创建时间',
                                    `update_time` bigint DEFAULT NULL COMMENT '修改时间',
                                    `workspace_id` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '项目id',
                                    `deleted` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '删除标识',
                                    `status_sort` varchar(10) COLLATE utf8mb3_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='需求统计表';