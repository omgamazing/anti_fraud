package com.example.controller;

import com.example.common.Result;
import com.example.entity.Case;
import com.example.service.CaseService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 反诈宣传前端请求接口
 */
@RestController
@RequestMapping("/case")
public class CaseController {

    @Resource
    private CaseService caseService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Case aCase) {
        caseService.add(aCase);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody Case aCase) {
        caseService.updateById(aCase);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        caseService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        caseService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Case aCase = caseService.selectById(id);
        return Result.success(aCase);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Case aCase) {
        List<Case> list = caseService.selectAll(aCase);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Case aCase,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Case> pageInfo = caseService.selectPage(aCase, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    @GetMapping("/selectTop4")
    public Result selectTop4() {
        List<Case> list = caseService.selectTop4();
        return Result.success(list);
    }

}
