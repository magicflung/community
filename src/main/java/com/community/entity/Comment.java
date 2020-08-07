package com.community.entity;

import java.util.Date;

/**
 * @author flunggg
 * @date 2020/7/29 10:46
 * @Email: chaste86@163.com
 */
public class Comment {
    private int id;
    private int userId;
    private int entityType; // 类型，比如帖子评论，帖子评论中的评论，未来还可以有课程的评论
    private int entityId; // 哪一张贴：帖子id，楼中楼：评论id
    private int targetId; // 回复的是谁，比如可以回复楼主，回复层主，回复层中的其他用户，这里主要是处理回复层中的其他用户
    private String content;
    private int status; // 0表示有效，1表示删除
    private Date createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", userId=" + userId +
                ", entityType=" + entityType +
                ", entityId=" + entityId +
                ", targetId=" + targetId +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}
