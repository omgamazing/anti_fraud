package com.example.mapper;

import com.example.entity.Collect;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CollectMapper {

    /**
     * 动态条件查询
     */
    @Select("<script>" +
            "select * from collect" +
            "<where>" +
            "<if test='userId != null'> and user_id = #{userId}</if>" +
            "<if test='caseId != null'> and case_id = #{caseId}</if>" +
            "</where>" +
            "</script>")
    List<Collect> selectAll(Collect collect);

    /**
     * 根据 userId 和 caseId 精确查询单条收藏记录
     */
    @Select("select * from collect where user_id = #{userId} and case_id = #{caseId}")
    Collect selectByUserIdAndCaseId(@Param("userId") Integer userId, @Param("caseId") Integer caseId);

    @Delete("delete from collect where id = #{id}")
    int deleteById(Integer id);

    @Insert("insert into collect (user_id, case_id) values (#{userId}, #{caseId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Collect collect);

    @Update("update collect set user_id = #{userId}, case_id = #{caseId} where id = #{id}")
    int updateById(Collect collect);

    @Select("select * from collect where id = #{id}")
    Collect selectById(Integer id);
}