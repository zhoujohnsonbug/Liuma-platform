package com.autotest.LiuMa.database.mapper;

import com.autotest.LiuMa.database.domain.StorySatistics;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StorySatisticsMapper {
    StorySatistics getStorySatisticsByStoryId(String storyId);

    void updateStorySatistics(StorySatistics storySatistics);


    void deleteStorySatistics(String shortId);

    List<StorySatistics> getAllStorySatistics();


    void saveStorySatistics(StorySatistics storySatistics);

}