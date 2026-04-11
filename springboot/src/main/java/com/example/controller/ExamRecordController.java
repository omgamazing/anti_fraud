package com.example.controller;

import com.example.common.Result;
import com.example.entity.ExamRecord;
import com.example.service.ExamRecordService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


/**
 * 考试记录 业务层
 */
@RestController
@RequestMapping("/exam/record")
public class ExamRecordController {

    @Resource
    private ExamRecordService examRecordService;

    /**
     * 获取用户该所有考试记录
     */
    @GetMapping("/statistics")
    public Result getUserRecords(Integer userId) {
        List<ExamRecord> simulationRecords = examRecordService.getUserRecords(userId);
        return Result.success(simulationRecords);
    }


    /**
     * 获取用户单个考试记录和记录详情
     */
    @GetMapping("/detail/{id}")
    public Result getRecordDetail(@PathVariable Integer id) {
        Map<String, Object> examRecordAndDetails=examRecordService.getRecordById(id);
        return Result.success(examRecordAndDetails);
    }


    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public com.example.common.Result selectPage(@RequestParam Integer userId,
                                                @RequestParam(required = false) String examTypeName,
                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<ExamRecord> pageInfo =examRecordService.selectPage(userId, examTypeName, pageNum, pageSize);
        return com.example.common.Result.success(pageInfo);
    }



}