package com.example.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import com.example.entity.Article;
import com.example.entity.Collect;
import com.example.mapper.ArticleMapper;
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
    private ArticleMapper articleMapper;

    public void add(Collect collect) {
        List<Collect> collects = collectMapper.selectAll(collect);
        if (CollectionUtil.isEmpty(collects)) {
            // 空的说明没有收藏记录
            collectMapper.insert(collect);
        } else {
            // 不是空的说明收藏过（而且只有一条数据），那么我们把这条收藏记录删掉即可
            collectMapper.deleteById(collects.get(0).getId());
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

    public List<Collect> selectAll(Collect collect) {
        return collectMapper.selectAll(collect);
    }

    public PageInfo<Collect> selectPage(Collect collect, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Collect> list = collectMapper.selectAll(collect);
        for (Collect dbCollect : list) {
            Article article = articleMapper.selectById(dbCollect.getArticleId());
            if (ObjUtil.isNotEmpty(article)) {
                dbCollect.setArticle(article);
            }
        }
        return PageInfo.of(list);
    }

}
