package com.autotest.LiuMa.database.mapper;

import com.autotest.LiuMa.database.domain.TesterOrder;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TesterOrderMapper {

    void updateTesterOrder(TesterOrder testerOrder);

    void addTesterOrder(TesterOrder testerOrder);

    List<TesterOrder> getAllTesterOrder();

    TesterOrder getTesterOrderById(Long id);

    TesterOrder getTesterOrderByJobId(String jobId);
}