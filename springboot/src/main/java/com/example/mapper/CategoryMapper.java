package com.example.mapper;

import com.example.entity.Category;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CategoryMapper {

    int insert(Category category);

    void updateById(Category category);

    void deleteById(Integer id);

    @Select("select * from `category` where id = #{id}")
    Category selectById(Integer id);

    List<Category> selectAll(Category category);

}
