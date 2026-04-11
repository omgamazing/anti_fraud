package com.example.mapper;

import com.example.entity.ExamRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExamRecordMapper {
    @Insert("INSERT INTO exam_record(" +
            "user_id, exam_type, exam_type_name, user_score, correct_count, wrong_count, duration) " +
            "VALUES(#{userId}, #{examType}, #{examTypeName}, #{userScore}, #{correctCount}, #{wrongCount}, #{duration})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertExamRecord(ExamRecord examRecord);

    /**
     * 查询用户所有考试记录
     */
    @Select("SELECT * FROM exam_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ExamRecord> findByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户ID查考试记录
     */
    @Select("SELECT * FROM exam_record WHERE id = #{id}")
    ExamRecord findById(@Param("id") Integer id);

    /**
     * 根据用户ID和输入框查询条件（考试类型）查询和分页
     */
    @Select("SELECT * FROM exam_record " +
            "WHERE user_id = #{userId} " +
            "AND (exam_type_name LIKE CONCAT('%', #{examTypeName}, '%') OR #{examTypeName} IS NULL OR #{examTypeName} = '') " +
            "ORDER BY create_time DESC")
    @Results({
            @Result(column = "user_id", property = "userId"),
            @Result(column = "exam_type", property = "examType"),
            @Result(column = "exam_type_name", property = "examTypeName"),
            @Result(column = "user_score", property = "userScore"),
            @Result(column = "correct_count", property = "correctCount"),
            @Result(column = "wrong_count", property = "wrongCount"),
            @Result(column = "create_time", property = "createTime")
    })
    List<ExamRecord> findByUserIdAndExamTypeName(@Param("userId") Integer userId, @Param("examTypeName") String examTypeName);




}
