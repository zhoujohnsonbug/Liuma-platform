package com.autotest.LiuMa.controller;

import com.autotest.LiuMa.database.domain.TesterOrder;
import com.autotest.LiuMa.dto.TesterOrderDTO;
import com.autotest.LiuMa.service.ScheduleJobService;
import com.autotest.LiuMa.service.TesterOrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@RequestMapping("/autotest/order")
public class TestOrderController {



    @Resource
    private TesterOrderService testerOrderService;


    @Resource
    private ScheduleJobService scheduleJobService;

    @PostMapping("/update")
    public void updateTesterOrder(@RequestBody TesterOrderDTO testerOrderDTO) throws Exception {
        System.out.println(testerOrderDTO);

        TesterOrder testerOrder = new TesterOrder();
        testerOrder.setId(testerOrderDTO.getId());
        testerOrder.setRanker(testerOrderDTO.getRanker());
        testerOrder.setRunTime(testerOrderDTO.getRunTime());
        testerOrder.setStatus(testerOrderDTO.getStatus());
        testerOrder.setDeleted(testerOrderDTO.getDeleted());
        testerOrder.setJobId(testerOrderDTO.getJobId());
//        StringBuilder sb = new StringBuilder();
//        List<String> frequency = testerOrderDTO.getFrequency();
//        if (frequency.size() > 0) {
//            for (String s : frequency) {
//                sb.append(s + "|");
//            }
//        }

//        testerOrder.setFrequency(sb.toString());
        testerOrder.setFrequency(testerOrderDTO.getFrequency());
        testerOrderService.updateTesterOrder(testerOrder);
    }

    @GetMapping("/getall")
    public  List<TesterOrderDTO> getAll(){
        ArrayList<TesterOrderDTO> testerOrderDTOS = new ArrayList<>();
        List<TesterOrder> allTesterOrder = testerOrderService.getAllTesterOrder();
        for (TesterOrder testerOrder : allTesterOrder) {
            TesterOrderDTO testerOrderDTO = new TesterOrderDTO();
            testerOrderDTO.setId(testerOrder.getId());
            testerOrderDTO.setRanker(testerOrder.getRanker());
            testerOrderDTO.setRunTime(testerOrder.getRunTime());
            testerOrderDTO.setJobId(testerOrder.getJobId());
//            testerOrderDTO.setFrequency(Stream.of(testerOrder.getFrequency().split("\\|")).collect(Collectors.toList()));
            testerOrderDTO.setFrequency(testerOrder.getFrequency());
            testerOrderDTO.setStatus(testerOrder.getStatus());
            testerOrderDTO.setDeleted(testerOrder.getDeleted());
            testerOrderDTOS.add(testerOrderDTO);
        }
        return testerOrderDTOS;
    }

    @GetMapping("/generate")
    public  String generate(){
        return scheduleJobService.generateMessage();
    }

    @PostMapping("create")
    public String createTesterOrder(@RequestBody TesterOrder testerOrder) throws Exception {
        testerOrderService.createTesterOrder(testerOrder);
        return "success";
    }


    @GetMapping("/delete")
    public  String delete(@RequestParam Long id) throws Exception {
        testerOrderService.deleteTesterOrder(id);
        return "success";
    }

    @PostMapping("/control")
    public void control(@RequestBody TesterOrder testerOrder) throws Exception {
        System.out.println(testerOrder);
        testerOrderService.control(testerOrder);
    }

}
