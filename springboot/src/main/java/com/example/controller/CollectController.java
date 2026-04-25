package com.example.controller;

import com.example.common.Result;
import com.example.entity.Collect;
import com.example.service.CollectService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 帖子收藏前端请求接口
 */
@RestController
@RequestMapping("/collect")
public class CollectController {

    @Resource
    private CollectService collectService;

    /**
     * 新增收藏
     */
    @PostMapping("/add")
    public Result add(@RequestBody Collect collect) {
        collectService.add(collect);
        return Result.success();
    }

    /**
     * 取消收藏（根据 userId 和 caseId）
     */
    @DeleteMapping("/cancel")
    public Result cancel(@RequestParam Integer userId, @RequestParam Integer caseId) {
        collectService.cancel(userId, caseId);
        return Result.success();
    }

    /**
     * 切换收藏状态
     */
    @PostMapping("/toggle")
    public Result toggle(@RequestBody Collect collect) {
        collectService.toggle(collect);
        return Result.success();
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/isCollected")
    public Result isCollected(@RequestParam Integer userId, @RequestParam Integer caseId) {
        boolean collected = collectService.isCollected(userId, caseId);
        return Result.success(collected);
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody Collect collect) {
        collectService.updateById(collect);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        collectService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        collectService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Collect collect = collectService.selectById(id);
        return Result.success(collect);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Collect collect) {
        List<Collect> list = collectService.selectAll(collect);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Collect collect,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Collect> pageInfo = collectService.selectPage(collect, pageNum, pageSize);
        return Result.success(pageInfo);
    }
}