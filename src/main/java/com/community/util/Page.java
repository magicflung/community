package com.community.util;

/**
 * 封装分页相关的信息
 * @author flunggg
 * @date 2020/7/19 18:08
 * @Email: chaste86@163.com
 */
public class Page {
    // 当前页
    private int current = 1;
    // 显示上限
    private int limit = 10;
    // 数据总数
    private int rows;
    // 查询路径（用于复用分页链接）
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current >= 1) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit >=1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return 获取当前页的起始行
     */
    public int getOffSet() {
        return current * limit - limit;
    }

    /**
     * @return 获取总页数
     */
    public int getTotal() {
        if(rows % limit == 0) {
            return rows / limit;
        } else {
            return rows / limit + 1;
        }
    }

    /**
     * @return 显示当前页左边的页数，这里只显示2页
     */
    public int getFrom() {
        int from = current - 2;
        return from >= 1 ? from : 1;
    }

    /**
     * 显示当前页右边的页数，这里只显示2页
     * @return
     */
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return to <= total ? to : total;
    }
}
