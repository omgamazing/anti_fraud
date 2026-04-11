package com.example.service;

import com.example.entity.ExamQuestion;
import com.example.entity.ExamRecord;
import com.example.entity.ExamRecordDetail;
import com.example.mapper.ExamQuestionMapper;
import com.example.mapper.ExamRecordDetailMapper;
import com.example.mapper.ExamRecordMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExamRecordService {
    @Resource
    private ExamRecordMapper examRecordMapper;
    @Resource
    private ExamRecordDetailMapper examRecordDetailMapper;
    @Resource
    private ExamQuestionMapper examQuestionMapper;

    // 获取用户的所有记录
    public List<ExamRecord> getUserRecords(Integer userId) {
        return examRecordMapper.findByUserId(userId);
    }


    // 获取用户单条记录和答题详情
    public Map<String, Object> getRecordById(Integer id) {
        Map<String, Object> result = new HashMap<>();
        // 1. 查考试记录
        ExamRecord record = examRecordMapper.findById(id);
        Integer recordId = record.getId();

        // 2. 查考试详情
        List<ExamRecordDetail> details = examRecordDetailMapper.findByRecordId(recordId);
// 3. 用 Map 组装详情，不加字段
        List<Map<String, Object>> detailsWithQuestion = new ArrayList<>();
        for (ExamRecordDetail detail : details) {
            ExamQuestion question = examQuestionMapper.findById(detail.getQuestionId());

            Map<String, Object> item = new HashMap<>();
            item.put("id", detail.getId());
            item.put("recordId", detail.getRecordId());
            item.put("questionId", detail.getQuestionId());
            item.put("questionTitle", detail.getQuestionTitle());
            item.put("questionType", detail.getQuestionType());
            item.put("userAnswer", detail.getUserAnswer());
            item.put("correctAnswer", detail.getCorrectAnswer());
            item.put("isCorrect", detail.getIsCorrect());
            item.put("score", detail.getScore());

            if (question != null) {
                item.put("optionA", question.getOptionA());
                item.put("optionB", question.getOptionB());
                item.put("optionC", question.getOptionC());
                item.put("optionD", question.getOptionD());
                item.put("analysis", question.getAnalysis());
            }

            detailsWithQuestion.add(item);
        }
        result.put("record", record);
        result.put("details", detailsWithQuestion);

        return result;
    }



    public PageInfo<ExamRecord> selectPage(Integer userId, String examTypeName, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ExamRecord> list = examRecordMapper.findByUserIdAndExamTypeName(userId,examTypeName);
        return PageInfo.of(list);
    }

}
