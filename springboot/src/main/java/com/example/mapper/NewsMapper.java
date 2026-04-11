package com.example.mapper;

import com.example.entity.News;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface NewsMapper {

    int insert(News news);

    void updateById(News news);

    void deleteById(Integer id);

    @Select("select * from `news` where id = #{id}")
    News selectById(Integer id);

    List<News> selectAll(News news);

    @Select("select * from `news` order by views desc limit 4")
    List<News> selectTop4();
}
