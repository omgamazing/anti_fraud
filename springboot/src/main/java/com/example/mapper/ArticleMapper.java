package com.example.mapper;

import com.example.entity.Article;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ArticleMapper {

    int insert(Article article);

    void updateById(Article article);

    void deleteById(Integer id);

    @Select("select * from `article` where id = #{id}")
    Article selectById(Integer id);

    List<Article> selectAll(Article article);

    @Select("select * from `article` where status = '审核通过' order by views desc limit 2")
    List<Article> selectTop2();
}
