package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Case;
import com.example.mapper.CaseMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 反诈百科业务层方法
 */
@Service
public class CaseService {

    @Resource
    private CaseMapper caseMapper;

    public void add(Case aCase) {
        aCase.setTime(DateUtil.now());
        caseMapper.insert(aCase);
    }

    public void updateById(Case aCase) {
        caseMapper.updateById(aCase);
    }

    public void deleteById(Integer id) {
        caseMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            caseMapper.deleteById(id);
        }
    }

    public Case selectById(Integer id) {
        return caseMapper.selectById(id);
    }

    public List<Case> selectAll(Case aCase) {
        return caseMapper.selectAll(aCase);
    }

    public PageInfo<Case> selectPage(Case aCase, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Case> list = caseMapper.selectAll(aCase);
        return PageInfo.of(list);
    }

    public List<Case> selectTop4() {
        return caseMapper.selectTop4();
    }
}
