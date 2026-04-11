package com.example.mapper;

import com.example.entity.ExamQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExamQuestionMapper {

    // 按题型随机抽取（综合板块用）
    @Select("SELECT * FROM exam_questions " +
            "WHERE status = 1 AND type = #{type} " +
            "ORDER BY RAND() LIMIT #{limit}")
    List<ExamQuestion> selectRandomByType(@Param("type") Integer type,
                                          @Param("limit") Integer limit);

    // 按分类和题型随机抽取（指定板块用）
    @Select("SELECT * FROM exam_questions " +
            "WHERE status = 1 AND category = #{category} AND type = #{type} " +
            "ORDER BY RAND() LIMIT #{limit}")
    List<ExamQuestion> selectRandomByCategoryAndType(@Param("category") Integer category,
                                                     @Param("type") Integer type,
                                                     @Param("limit") Integer limit);

    // 新增：根据ID查询单个题目
    @Select("SELECT * FROM exam_questions WHERE id = #{id}")
    ExamQuestion findById(@Param("id") Integer id);

}
