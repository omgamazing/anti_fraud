package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Case;
import com.example.entity.ExamQuestion;
import com.example.entity.ExamRecord;
import com.example.entity.ExamRecordDetail;
import com.example.mapper.ExamQuestionMapper;
import com.example.mapper.ExamRecordMapper;
import com.example.mapper.ExamRecordDetailMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExamQuestionService {

    @Resource
    private ExamQuestionMapper examQuestionMapper;
    @Resource
    private ExamRecordMapper examRecordMapper;
    @Resource
    private ExamRecordDetailMapper examRecordDetailMapper;
    // 题目数量配置
    private static final int SINGLE_COUNT = 5;   // 单选5道
    private static final int MULTI_COUNT = 2;    // 多选2道
    private static final int JUDGE_COUNT = 3;    // 判断3道

    // 分值配置
    private static final int SINGLE_SCORE = 6;   // 单选6分
    private static final int MULTI_SCORE = 20;   // 多选20分
    private static final int JUDGE_SCORE = 10;   // 判断10分


    /**
     * 生成试卷
     * @param examType 分类：1反诈基础 2信息网络安全 3资金应急处置，4表示综合
     */
    public Map<String, Object> generatePaper(Integer examType){

        Map<String, Object> paper = new HashMap<>();
        List<ExamQuestion> questions = new ArrayList<>();

        //根据前端用户选择哪个板块调用
        if (examType == 4) {
            questions.addAll(examQuestionMapper.selectRandomByType(1, SINGLE_COUNT));
            questions.addAll(examQuestionMapper.selectRandomByType(2, MULTI_COUNT));
            questions.addAll(examQuestionMapper.selectRandomByType(3, JUDGE_COUNT));
        } else {
            questions.addAll(examQuestionMapper.selectRandomByCategoryAndType(examType, 1, SINGLE_COUNT));
            questions.addAll(examQuestionMapper.selectRandomByCategoryAndType(examType, 2, MULTI_COUNT));
            questions.addAll(examQuestionMapper.selectRandomByCategoryAndType(examType, 3, JUDGE_COUNT));
        }


        paper.put("questions", questions);
        paper.put("questionCount", questions.size());
        paper.put("examType", examType);
        paper.put("examTypeName", getExamTypeName(examType));

        return paper;

    }



    /**
     * 提交试卷后,计算成绩并保存记录
     * @param userId 用户ID
     * @param examType 考试分类
     * @param questions 题目列表
     * @param userAnswers 用户答案列表
     * @param duration 考试用时
     */
    public Map<String, Object> submitPaper(Integer userId, Integer examType,
                                           List<ExamQuestion> questions,
                                           List<String> userAnswers,
                                           Integer duration) {

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> details = new ArrayList<>();

        int totalScore = 0;
        int correctCount = 0;

        // 1. 计算得分
        for (int i = 0; i < questions.size(); i++) {
            ExamQuestion q = questions.get(i);
            String userAnswer = userAnswers.get(i);

            // 判断是否正确
            boolean isCorrect = isCorrect(q.getAnswer(), userAnswer, q.getType());

            // 计算分数
            int score = 0;
            if (isCorrect) {
                score = getScoreByType(q.getType());
                totalScore += score;
                correctCount++;
            }

            // 记录每题详情
            Map<String, Object> detail = new HashMap<>();
            detail.put("questionId", q.getId());
            detail.put("title", q.getTitle());
            detail.put("questionType", q.getType());
            detail.put("userAnswer", userAnswer);
            detail.put("correctAnswer", q.getAnswer());
            detail.put("isCorrect", isCorrect);
            detail.put("score", score);
            detail.put("analysis", q.getAnalysis());
            details.add(detail);
        }

        // 2. 保存考试记录
        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setExamType(examType);
        record.setExamTypeName(getExamTypeName(examType));
        record.setUserScore(totalScore);
        record.setCorrectCount(correctCount);
        record.setWrongCount(questions.size() - correctCount);
        record.setDuration(duration);
        examRecordMapper.insertExamRecord(record);

        // 3. 保存考试详情
        for (Map<String, Object> detail : details) {
            ExamRecordDetail detailEntity = new ExamRecordDetail();
            detailEntity.setRecordId(record.getId());
            detailEntity.setQuestionId((Integer) detail.get("questionId"));
            detailEntity.setQuestionTitle((String) detail.get("title"));
            detailEntity.setQuestionType((Integer) detail.get("questionType"));
            detailEntity.setUserAnswer((String) detail.get("userAnswer"));
            detailEntity.setCorrectAnswer((String) detail.get("correctAnswer"));
            detailEntity.setIsCorrect((Boolean) detail.get("isCorrect") ? 1 : 0);
            detailEntity.setScore((Integer) detail.get("score"));
            examRecordDetailMapper.insertExamRecordDetail(detailEntity);
        }

        result.put("totalScore", totalScore);
        result.put("correctCount", correctCount);
        result.put("wrongCount", questions.size() - correctCount);
        result.put("recordId", record.getId());
        result.put("details", details);

        return result;
    }

    /**
     * 判断答案是否正确
     */
    private boolean isCorrect(String correctAnswer, String userAnswer, Integer type) {
        if (userAnswer == null || userAnswer.isEmpty()) {
            return false;
        }

        if (type == 2) { // 多选题
            // 比较两个集合是否相等（顺序无关）
            Set<String> correctSet = new HashSet<>(Arrays.asList(correctAnswer.split(",")));
            Set<String> userSet = new HashSet<>(Arrays.asList(userAnswer.split(",")));
            return correctSet.equals(userSet);
        } else { // 单选、判断
            return correctAnswer.equalsIgnoreCase(userAnswer);
        }
    }

    /**
     * 根据题型获取分值
     */
    private int getScoreByType(Integer type) {
        return switch (type) {
            case 1 -> SINGLE_SCORE;   // 6分
            case 2 -> MULTI_SCORE;    // 20分
            case 3 -> JUDGE_SCORE;    // 10分
            default -> 0;
        };
    }

    /**
     * 获取分类名称
     */
    private String getExamTypeName(Integer examType) {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "反诈基础");
        map.put(2, "信息网络安全");
        map.put(3, "资金应急处置");
        map.put(4, "综合测试");
        return map.getOrDefault(examType, "未知");
    }

    /**
     *
     * 管理员——新增
     */
    public void add(ExamQuestion examQuestion) {
        examQuestionMapper.insert(examQuestion);
    }

    /**
     * 管理员——修改
     */
    public void updateById(ExamQuestion examQuestion) {
        examQuestionMapper.updateById(examQuestion);
    }

    /**
     * 管理员——单个删除
     */
    public void deleteById(Integer id) {
        examQuestionMapper.deleteById(id);
    }

    /**
     * 管理员——批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            examQuestionMapper.deleteById(id);
        }
    }

    /**
     * 单个查询
     */
    public ExamQuestion selectById(Integer id) {
        return examQuestionMapper.selectById(id);
    }

    /**
     * 分页查询
     */
    public PageInfo<ExamQuestion> selectPage(String title, Integer type, Integer category, Integer status,Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ExamQuestion> list = examQuestionMapper.selectByCondition(title, type, category,status);
        return PageInfo.of(list);
    }




    /*public List<ExamQuestion> selectAll(ExamQuestion examQuestion) {
        return examQuestionMapper.selectAll(examQuestion);
    }*/


}
