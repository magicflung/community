package com.community.filter;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用前缀树来过滤敏感词
 * @author flunggg
 * @date 2020/7/27 11:26
 * @Email: chaste86@163.com
 */
@Component
public class SensitiveFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensitiveFilter.class);

    private final static String REPLACE_WORDS = "***";

    // 根节点
    private TireNode root = new TireNode();

    /**
     * 在SensitiveFilter实例化后会调用，来构造前缀树
     * 这里对于敏感词的存放：
     *  - 可以放在一个文件
     *  - 可以放在数据库
     */
    @PostConstruct
    private void init() {
        try(
                // 从类路径下加载文件
                InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                // 因为是文本文件，最好转换成字符流，这里使用缓存流，效率比较高。
                BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
        ) {
            String keyword;
            while((keyword = reader.readLine()) != null) { // 每次读取一行
                addKeyword(keyword);
            }
        } catch (IOException e) {
            LOGGER.error("敏感词文件读取失败：" + e.getMessage());
        }
    }

    // 把一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TireNode cur = root;
        for(int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            // 查询是否有子节点
            TireNode subNode = cur.getTireNode(c);

            // 如果没有，则创建子节点并添加
            if(subNode == null) {
                subNode = new TireNode();
                cur.addTireNode(c, subNode);
            }

            // 移动到下一个子节点
            cur = subNode;

            // 当到达敏感词最后一个字符，标记为结束
            if(i == keyword.length() - 1) {
                cur.setKeywordEnd(true);
            }
        }
    }


    // 底层使用数据结构来存储敏感词
    private class TireNode {
        // 敏感词结束标记
        private boolean isKeywordEnd = false;
        // 子节点使用Map存储, CRUD都是O(1)
        // 经典的前缀树使用边来放字符，而不是节点
        // key: 当前敏感词的一个字符的下一个字符，也可以理解为边
        // value：下一个字符的节点
        private Map<Character, TireNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addTireNode(Character c, TireNode subNode) {
            subNodes.put(c, subNode);
        }

        public TireNode getTireNode(Character c) {
            return subNodes.get(c);
        }
    }

    /**
     *
     * @param text 代过滤敏感词的文件
     * @return 过滤敏感词后的文本
     */
    public String filter(String text) {
        if(StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1：指向树
        TireNode curNode = root;
        // 指针2：表示的是一串子串，范围是[begin, position]
        int begin = 0;
        // 指针3：如果字符串的一个字符为敏感词的第一个字符，那么position就需要遍历begin后的字符与敏感词的字符，一一比较
        int position = 0;
        // 结果集
        StringBuffer result = new StringBuffer();

        while(position < text.length()) {

            // 刚开始position = begin = 0
            // 但是begin可能不会去一一遍历每一个字符，而是定义到某一子串的开头
            // 然后以position来移动判断
            // 所以就使用position遍历，但是每次需要根据position的位置去维护begin位置
            // 画画图最好
            char c = text.charAt(position);

            // 因为可以这样：☆赌☆博☆， 所以得检查c是否为符号(☆等)
            if(isSymbol(c)) {
                // 因为每次过滤敏感词后都会把指针1重新指向根节点
                // 如果指针1在根节点，说明还没进敏感词的判断，直接把该符号记录到结果
                if(curNode == root) {
                    result.append(c);
                    begin++;
                }
                // 否则，说明此时已经进入前缀树敏感词过滤
                // 此时该符号处于敏感词中间
                // 所以begin不移动，只移动position
                position++;
                // 这轮已经处理好了，进入下一轮循环
                continue;
            }

            // 前缀树的字符存储在边上，节点不存信息，所以可以通过字符去查询有没有这条边，有的话拿出节点
            // 检查下一级节点
            // 情况一：根节点没有存东西，要查询就得去它的下级节点
            // 情况二：可能已经进入前缀树，上一级已经处理好，这轮处理它的下一级
            curNode = curNode.getTireNode(c);
            if(curNode == null) {
                // 如果没有，说明[begin,position]字符串不是敏感词
                // 但是[begin+1,position]就不一定了，所以只能加入begin位置的字符
                result.append(text.charAt(begin));
                // 下一个位置
                position = ++begin;
                // 比如出现 "赌啊！"，上一轮curNode指向 "赌"这个节点，此时查询"啊"，发现没有，不是敏感词
                // 所以需要重新指向根节点
                curNode = root;
            } else if(curNode.isKeywordEnd()) {
                // 到达敏感词结尾，那么把[begin,position]之间的字符过滤
                // 而这里直接跳过[begin,position]就行，并拼上"***"
                result.append(REPLACE_WORDS);
                // 下一个位置
                begin = ++position;
                // 重新指向根节点
                curNode = root;
            } else if(position + 1 == text.length()) {
                // 特殊情况，出现在结尾
                // 就是虽然[begin,position]指向的字符串存在在前缀树中，但是不以position结尾，position+1就越界了
                // 此时还得判断[begin+1,position]是否为敏感词
                result.append(text.charAt(begin));
                // 下一个位置
                position = ++begin;
                // 重新指向根节点
                curNode = root;
            } else {
                // 说明进入到了敏感词判断但没到敏感词结尾，继续判断下一个字符
                position++;
            }
        }

        // 情况：比如到达最后2个字符，第一个是敏感词字符之一，而第二个不是，此时position就走完了
        // 得将最后一批字符计入
        return begin < text.length() ? result.append(text.substring(begin)).toString() : result.toString();
    }

    /**
     *
     * @param c
     * @return 判断是否符号
     */
    private boolean isSymbol(char c) {
        // 0x2E80~0x9FFF 属于东亚文字范围
        // CharUtils.isAsciiAlphanumeric判断是否为 普通字符，如果是返回true
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }
}
