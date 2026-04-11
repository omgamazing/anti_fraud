package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HtmlUtil;
import com.example.entity.Article;
import com.example.mapper.ArticleMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 论坛帖子业务层方法
 */
@Service
public class ArticleService {

    @Resource
    private ArticleMapper articleMapper;

    public void add(Article article) {
        article.setTime(DateUtil.now());
        articleMapper.insert(article);
    }

    public void updateById(Article article) {
        articleMapper.updateById(article);
    }

    public void deleteById(Integer id) {
        articleMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            articleMapper.deleteById(id);
        }
    }

    public Article selectById(Integer id) {
        return articleMapper.selectById(id);
    }

    public List<Article> selectAll(Article article) {
        return articleMapper.selectAll(article);
    }

    public PageInfo<Article> selectPage(Article article, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Article> list = articleMapper.selectAll(article);
        for (Article dbArticle : list) {
            dbArticle.setDescription(HtmlUtil.cleanHtmlTag(dbArticle.getContent()));
        }
        return PageInfo.of(list);
    }

    public List<Article> selectTop2() {
        return articleMapper.selectTop2();
    }
}
