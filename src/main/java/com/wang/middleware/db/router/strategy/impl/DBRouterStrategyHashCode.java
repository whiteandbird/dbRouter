package com.wang.middleware.db.router.strategy.impl;

import com.wang.middleware.db.router.DBContextHolder;
import com.wang.middleware.db.router.DBRouterConfig;
import com.wang.middleware.db.router.strategy.IDBRouterStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: whiteandbird
 * @Descripter:
 * @Date: 2022:07:17  20:47
 */
public class DBRouterStrategyHashCode implements IDBRouterStrategy {

    private Logger logger = LoggerFactory.getLogger(DBRouterStrategyHashCode.class);

    private DBRouterConfig dbRouterConfig;


    public DBRouterStrategyHashCode(DBRouterConfig dbRouterConfig){
        this.dbRouterConfig = dbRouterConfig;
    }

    @Override
    public void doRouter(String dbKeyAttr) {
        // 库数 乘以 表的数量
        int size = dbRouterConfig.getDbCount() * dbRouterConfig.getTbCount();

        // 扰动函数 使其更加散列
        int idx = (size - 1) & (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16));

        // 因为默认库设置为0 所以会对库进行加+1操作
        int dbIdx = idx / dbRouterConfig.getTbCount() +1;
        int tbIdx = idx - dbRouterConfig.getTbCount() * (dbIdx - 1);

        DBContextHolder.setDBKey(String.format("%02d", dbIdx));
        DBContextHolder.setTBKey(String.format("%02d", tbIdx));

        logger.debug("设置数据库路由 dbIdx:{}  tbIdx:{}", dbIdx, tbIdx);

    }

    @Override
    public void setDbkey(int dbIdx) {
        DBContextHolder.setDBKey(String.format("%02d", dbIdx));
    }

    @Override
    public void setTbKey(int tbIdx) {
        DBContextHolder.setTBKey(String.format("%02d", tbIdx));
    }

    @Override
    public int dbCount() {
        return dbRouterConfig.getDbCount();
    }

    @Override
    public int tbCount() {
        return dbRouterConfig.getTbCount();
    }

    @Override
    public void clear() {
        DBContextHolder.clearDBKey();
        DBContextHolder.clearTBKey();
    }
}
