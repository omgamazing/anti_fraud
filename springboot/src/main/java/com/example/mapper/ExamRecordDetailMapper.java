package com.example.mapper;

import com.example.entity.ExamRecordDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamRecordDetailMapper {
    /**
     * 插入考试答题数据
     */
    @Insert("INSERT INTO exam_record_detail(" +
            "record_id, question_id, question_title, question_type, user_answer, correct_answer, is_correct, score) " +
            "VALUES(#{recordId}, #{questionId}, #{questionTitle}, #{questionType}, #{userAnswer}, #{correctAnswer}, #{isCorrect}, #{score})")
    int insertExamRecordDetail(ExamRecordDetail detail);


    /**
     * 根据recordId 查询考试详情
     */
    @Select("SELECT * FROM exam_record_detail WHERE record_id = #{recordId}")
    List<ExamRecordDetail> findByRecordId(@Param("recordId") Integer recordId);
}
