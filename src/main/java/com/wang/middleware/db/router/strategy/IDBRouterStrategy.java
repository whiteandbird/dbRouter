package com.wang.middleware.db.router.strategy;

public interface IDBRouterStrategy {

    /**
     * 路由计算
     * @param dbKeyAttr
     */
    void doRouter(String dbKeyAttr);

    /**
     * 手动设置分库路由
     * @param dbIdx
     */
    void setDbkey(int dbIdx);

    /**
     * 手动设置分表路由
     * @param tbIdx
     */
    void setTbKey(int tbIdx);

    /**
     * 获取分库数
     * @return
     */
    int dbCount();

    /**
     * 获取分表数
     * @return
     */
    int tbCount();

    /**
     * 清楚路由
     */
    void clear();
}
