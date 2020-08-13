package com.community.service;

import com.community.dao.DiscussPostMapper;
import com.community.entity.DiscussPost;
import com.community.filter.SensitiveFilter;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author flunggg
 * @date 2020/7/19 14:46
 * @Email: chaste86@163.com
 */
@Service
public class DiscussPostService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    // 引入本地缓存Caffeine
    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine的核心接口：Cache：子接口：LoadingCache（同步的），AsyncLoadingCache（异步的）
    // 帖子列表的缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;
    // 帖子总数的缓存
    private LoadingCache<Integer, Integer> postCountCache;

    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    // 查询数据（如果只有1级缓存那么就会去DB）
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if(key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误！");
                        }
                        String[] param = key.split(":");
                        if(param == null || param.length != 2) {
                            throw new IllegalArgumentException("参数错误！");
                        }

                        int offset = Integer.parseInt(param[0]);
                        int limit = Integer.parseInt(param[1]);

                        // 这里可加上二级缓存（Redis）


                        // 到DB查询
                        LOGGER.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });
        // 初始化帖子数量缓存
        postCountCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        // 到DB查询
                        LOGGER.debug("load post rows from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    /**
     * @param userId 用户id，如果为0则表示全部用户
     * @param offset 行数
     * @param limit 一页多少条
     * // 代码重构
     * @param orderMode 0:表示按type倒序然后再轮到create_time倒序；1：表示按照按type倒序然后score倒序最后再轮到create_time倒序
     * @return 返回一个论贴列表
     */
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {

        //
        // 添加缓存优化
        // 缓存条件：userId不传，orderMode=0，然后是第一页。
        // 所以与offset和limit有关
        if(userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }

        LOGGER.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    /**
     * @param userId 用户id，如果为0则表示全部用户
     * @return 返回论贴一共有多少条
     */
    public int findDiscussPostRows(int userId) {

        // 添加缓存优化
        // 主要是帖子列表的，不是单个用户的
        if(userId == 0) {
            // 这个userId肯定是0
            return postCountCache.get(userId);
        }

        LOGGER.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost discussPost) {
        // 需要进行标签转义
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        // 敏感词过滤
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }
}
