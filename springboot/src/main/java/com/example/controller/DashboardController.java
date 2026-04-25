package com.example.controller;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.example.common.Result;
import com.example.entity.*;
import com.example.service.*;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private ArticleService articleService;
    @Resource
    private CaseService caseService;
    @Resource
    private UserService userService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private SimulationRecordService simulationRecordService;
    @Resource
    private ExamRecordService examRecordService;

    /**
     * 统计数据：用户帖子总数、反诈百科数量、AI模拟次数、平台用户总数
     */
    @GetMapping("/base")
    public Result base() {
        Map<String, Integer> map = new HashMap<>();

        // 用户帖子总数（审核通过的）
        List<Article> articles = articleService.selectAll(new Article()).stream()
                .filter(x -> "审核通过".equals(x.getStatus()))
                .collect(Collectors.toList());
        map.put("article", articles.size());

        // 反诈百科数量
        map.put("case", caseService.selectAll(new Case()).size());

        // AI模拟次数（从模拟记录表统计）
        map.put("simulation", simulationRecordService.countAll());

        // 平台用户总数
        map.put("user", userService.selectAll(new User()).size());

        return Result.success(map);
    }

    /**
     * AI模拟趋势图（最近一个月）
     */
    @GetMapping("/simulationTrend")
    public Result simulationTrend() {
        Map<String, Object> map = new HashMap<>();
        List<Integer> yList = new ArrayList<>();

        // 获取最近一个月的日期
        Date today = new Date();
        DateTime start = DateUtil.offsetDay(today, -29); // 最近7天（包含今天）
        List<String> xList = DateUtil.rangeToList(start, today, DateField.DAY_OF_YEAR)
                .stream()
                .map(DateUtil::formatDate)
                .collect(Collectors.toList());

        // 获取最近一个月每天的模拟次数
        for (String date : xList) {
            int count = simulationRecordService.countByDate(date);
            yList.add(count);
        }

        map.put("x", xList);
        map.put("y", yList);
        return Result.success(map);
    }

    /**
     * 帖子分类占比（饼图1）
     */
    @GetMapping("/articleCategory")
    public Result articleCategory() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Category> categories = categoryService.selectAll(new Category());
        List<Article> articles = articleService.selectAll(new Article()).stream()
                .filter(x -> "审核通过".equals(x.getStatus()))
                .collect(Collectors.toList());

        for (Category category : categories) {
            long count = articles.stream()
                    .filter(x -> category.getId().equals(x.getCategoryId()))
                    .count();
            if (count > 0) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", category.getName());
                map.put("value", count);
                list.add(map);
            }
        }
        return Result.success(list);
    }

    /**
     * 考试分类占比（饼图2）
     */
    @GetMapping("/examCategory")
    public Result examCategory() {
        List<Map<String, Object>> list = new ArrayList<>();

        // 从考试记录表统计各类型考试次数
        List<Map<String, Object>> examStats = examRecordService.getExamTypeCount();

        for (Map<String, Object> stat : examStats) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", stat.get("name"));
            map.put("value", stat.get("value"));
            list.add(map);
        }

        return Result.success(list);
    }
}