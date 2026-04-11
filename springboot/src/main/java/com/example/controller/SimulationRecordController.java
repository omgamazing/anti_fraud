package com.example.controller;

import com.example.common.Result;
import com.example.entity.SimulationRecord;
import com.example.service.SimulationRecordService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/record")
public class SimulationRecordController {

    @Resource
    private SimulationRecordService simulationRecordService;

    /**
     * 保存
     */
    @PostMapping("/save")
    public Result saveRecord(@RequestBody SimulationRecord simulationRecord) {
        simulationRecord.setCreateTime(LocalDateTime.now());
        simulationRecordService.saveRecord(simulationRecord);
        return Result.success(simulationRecord);
    }

    /**
     * 获取用户该所有模拟记录
     */
    @GetMapping("/statistics")
    public Result getUserRecords(Integer userId) {
        List<SimulationRecord> simulationRecords = simulationRecordService.getUserRecords(userId);
        return Result.success(simulationRecords);
    }

    // 获取单条记录详情
    @GetMapping("/detail/{id}")
    public Result getRecordDetail(Integer id) {
        SimulationRecord simulationRecord=simulationRecordService.getRecordById(id);
        return Result.success(simulationRecord);
    }

    /**
     * 统计
     */
    @GetMapping("/count")
    public Result getUserStats(Integer userId) {
        Map<String, Object> stats = simulationRecordService.getUserStats(userId);
        return Result.success(stats);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public com.example.common.Result selectPage(@RequestParam Integer userId,
                                                @RequestParam(required = false) String scene,
                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<SimulationRecord> pageInfo =simulationRecordService.selectPage(userId, scene, pageNum, pageSize);
        return com.example.common.Result.success(pageInfo);
    }



}