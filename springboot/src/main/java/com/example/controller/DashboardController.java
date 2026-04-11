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
    private NewsService newsService;
   /* @Resource
    private ActivityService activityService;*/
    @Resource
    private UserService userService;
    @Resource
    private CategoryService categoryService;


    @GetMapping("/base")
    public Result base() {
        Map<String, Integer> map = new HashMap<>();
        List<Article> articles = articleService.selectAll(new Article()).stream().filter(x -> "审核通过".equals(x.getStatus())).collect(Collectors.toList());
        map.put("article", articles.size());
        map.put("news", newsService.selectAll(new News()).size());
       // map.put("activity", activityService.selectAll(new Activity()).size());
        map.put("user", userService.selectAll(new User()).size());
        return Result.success(map);
    }

    @GetMapping("/line")
    public Result line() {
        Map<String, Object> map = new HashMap<>();
        List<Long> yList = new ArrayList<>();

        // 获取最近7天的数据（年-月-日）放在xList里
        Date today = new Date();
        DateTime start = DateUtil.offsetDay(today, -7);
        List<String> xList = DateUtil.rangeToList(start, today, DateField.DAY_OF_YEAR).stream().map(DateUtil::formatDate).toList();



        map.put("x", xList);
        map.put("y", yList);
        return Result.success(map);
    }

    @GetMapping("/pie1")
    public Result pie1() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Category> categories = categoryService.selectAll(new Category());
        List<Article> articles = articleService.selectAll(new Article());
        for (Category category : categories) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", category.getName());
            map.put("value", articles.stream().filter(x -> x.getCategoryId().equals(category.getId())).count());
            list.add(map);
        }
        return Result.success(list);
    }

    @GetMapping("/pie2")
    public Result pie2() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Category> categories = categoryService.selectAll(new Category());
        //List<Activity> activities = activityService.selectAll(new Activity());
        for (Category category : categories) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", category.getName());
            //map.put("value", activities.stream().filter(x -> x.getCategoryId().equals(category.getId())).count());
            list.add(map);
        }
        return Result.success(list);
    }
}
