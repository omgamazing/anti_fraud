package com.example.mapper;

import com.example.entity.Case;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CaseMapper {

    int insert(Case aCase);

    void updateById(Case aCase);

    void deleteById(Integer id);

    @Select("select * from `case` where id = #{id}")
    Case selectById(Integer id);

    List<Case> selectAll(Case aCase);

    @Select("select * from `case` order by views desc limit 4")
    List<Case> selectTop4();


}
