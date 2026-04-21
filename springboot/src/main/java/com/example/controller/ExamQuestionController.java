package com.example.controller;

import com.example.common.Result;
import com.example.entity.Case;
import com.example.entity.ExamQuestion;
import com.example.service.ExamQuestionService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 知识考核前端请求接口
 */
@RestController
@RequestMapping("/exam")
public class ExamQuestionController {
    @Resource
    private ExamQuestionService examQuestionService;

    /**
     * 生成试卷 /exam/generatePaper
     */
    @PostMapping("/generatePaper") //处理POST请求
    public Result generatePaper(@RequestBody Map<String, Object> params) {
        Integer examType = (Integer) params.get("examType");
        Map<String, Object> paper=examQuestionService.generatePaper(examType);
        return Result.success(paper);
    }


    /**
     * 计算成绩 /exam/submitPaper
     */
    @PostMapping("/submitPaper")
    public Result submitPaper(@RequestBody Map<String, Object> params) {
        Integer userId = (Integer) params.get("userId");
        Integer examType = (Integer) params.get("examType");
        Integer duration = (Integer) params.get("duration");

        // 获取 questions（可能是 List<Map>）
        List<Map<String, Object>> questionsMap = (List<Map<String, Object>>) params.get("questions");

        // 获取 userAnswers（可能是 List<String> 或 List<Object>）
        List<?> rawAnswers = (List<?>) params.get("userAnswers");
        List<String> userAnswers = new ArrayList<>();

        for (Object obj : rawAnswers) {
            if (obj instanceof List) {
                // 多选题答案：["A","B"] -> "A,B"
                List<?> list = (List<?>) obj;
                userAnswers.add(String.join(",", list.stream().map(Object::toString).toArray(String[]::new)));
            } else {
                userAnswers.add(obj.toString());
            }
        }

        // 转换 questions
        List<ExamQuestion> questions = new ArrayList<>();
        for (Map<String, Object> map : questionsMap) {
            ExamQuestion q = new ExamQuestion();
            q.setId((Integer) map.get("id"));
            q.setTitle((String) map.get("title"));
            q.setType((Integer) map.get("type"));
            q.setAnswer((String) map.get("answer"));
            q.setAnalysis((String) map.get("analysis"));
            questions.add(q);
        }

        Map<String, Object> result = examQuestionService.submitPaper(userId, examType, questions, userAnswers, duration);
        return Result.success(result);
    }
    
  
    /**
     * 管理员——新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody ExamQuestion examQuestion) {
        examQuestionService.add(examQuestion);
        return Result.success();
    }

    /**
     * 管理员——修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody ExamQuestion examQuestion) {
        examQuestionService.updateById(examQuestion);
        return Result.success();
    }

    /**
     * 管理员——单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        examQuestionService.deleteById(id);
        return Result.success();
    }

    /**
     * 管理员——批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        examQuestionService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 管理员——单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        ExamQuestion examQuestion = examQuestionService.selectById(id);
        return Result.success(examQuestion);
    }

    /**
     * 管理员——分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(required = false) String title,
                             @RequestParam(required = false) Integer type,
                             @RequestParam(required = false) Integer category,
                             @RequestParam(required = false) Integer status,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<ExamQuestion> pageInfo = examQuestionService.selectPage(title, type, category,status,pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /*@GetMapping("/selectTop4")
    public Result selectTop4() {
        List<Case> list = examQuestionService.selectTop4();
        return Result.success(list);
    }*/



}
