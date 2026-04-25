package com.example.service;

import cn.hutool.core.util.ObjUtil;
import com.example.entity.Case;
import com.example.entity.Collect;
import com.example.mapper.CaseMapper;
import com.example.mapper.CollectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 帖子收藏业务层方法
 */
@Service
public class CollectService {

    @Resource
    private CollectMapper collectMapper;
    @Resource
    private CaseMapper caseMapper;

    /**
     * 添加收藏（如果已存在则不操作）
     */
    public void add(Collect collect) {
        // 检查是否已收藏
        Collect existing = collectMapper.selectByUserIdAndCaseId(collect.getUserId(), collect.getCaseId());
        if (existing == null) {
            collectMapper.insert(collect);
        }
    }

    /**
     * 取消收藏（根据 userId 和 caseId）
     */
    public void cancel(Integer userId, Integer caseId) {
        Collect existing = collectMapper.selectByUserIdAndCaseId(userId, caseId);
        if (existing != null) {
            collectMapper.deleteById(existing.getId());
        }
    }

    /**
     * 切换收藏状态（有则删，无则加）
     */
    public void toggle(Collect collect) {
        Collect existing = collectMapper.selectByUserIdAndCaseId(collect.getUserId(), collect.getCaseId());
        if (existing != null) {
            collectMapper.deleteById(existing.getId());
        } else {
            collectMapper.insert(collect);
        }
    }

    public void updateById(Collect collect) {
        collectMapper.updateById(collect);
    }

    public void deleteById(Integer id) {
        collectMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            collectMapper.deleteById(id);
        }
    }

    public Collect selectById(Integer id) {
        return collectMapper.selectById(id);
    }

    /**
     * 查询所有（支持动态条件）
     */
    public List<Collect> selectAll(Collect collect) {
        return collectMapper.selectAll(collect);
    }

    /**
     * 分页查询（联表解决 N+1 问题）
     */
    public PageInfo<Collect> selectPage(Collect collect, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Collect> list = collectMapper.selectAll(collect);

        // 批量查询 Case，解决 N+1 问题
        if (list != null && !list.isEmpty()) {
            for (Collect dbCollect : list) {
                Case aCase = caseMapper.selectById(dbCollect.getCaseId());
                if (ObjUtil.isNotEmpty(aCase)) {
                    dbCollect.setCaseObj(aCase);
                }
            }
        }
        return PageInfo.of(list);
    }

    /**
     * 检查是否已收藏
     */
    public boolean isCollected(Integer userId, Integer caseId) {
        Collect existing = collectMapper.selectByUserIdAndCaseId(userId, caseId);
        return existing != null;
    }
}