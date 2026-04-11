package com.example.mapper;

import com.example.entity.Article;
import com.example.entity.SimulationRecord;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Mapper
public interface SimulationRecordMapper {

    // 插入记录
    @Insert("INSERT INTO simulation_records (user_id, session_id, scene, result, rounds, duration, messages, create_time) " +
            "VALUES (#{userId}, #{sessionId}, #{scene}, #{result}, #{rounds}, #{duration}, #{messages}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SimulationRecord record);

    // 根据用户ID查询所有记录（按时间倒序）
    //MyBatis 不知道 user_id 对应 userId，所以需要用 @Result 告诉它
    @Select("SELECT * FROM simulation_records WHERE user_id = #{userId} ORDER BY create_time DESC")
    @Results({
            @Result(column = "user_id", property = "userId"),
            @Result(column = "session_id", property = "sessionId"),
            @Result(column = "create_time", property = "createTime")
    })
    List<SimulationRecord> findByUserId(@Param("userId") Integer userId);

    // 根据用户ID和输入框查询条件（场景）查询和分页
    @Select("SELECT * FROM simulation_records " +
            "WHERE user_id = #{userId} " +
            "AND (scene LIKE CONCAT('%', #{scene}, '%') OR #{scene} IS NULL OR #{scene} = '') " +
            "ORDER BY create_time DESC")
    List<SimulationRecord> findByUserIdAndScene(@Param("userId") Integer userId, @Param("scene") String scene);

    // 统计用户的成功/失败次数
    @Select("SELECT COUNT(*) FROM simulation_records WHERE user_id = #{userId} AND result = #{result}")
    long countByUserIdAndResult(@Param("userId") Integer userId, @Param("result") String result);

    // 根据ID查询单条记录（用于详情）
    @Select("SELECT * FROM simulation_records WHERE id = #{id}")
    @Results({
            @Result(column = "user_id", property = "userId"),
            @Result(column = "session_id", property = "sessionId"),
            @Result(column = "create_time", property = "createTime")
    })
    SimulationRecord findById(@Param("id") Integer id);


}