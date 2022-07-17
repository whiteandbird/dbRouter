package com.wang.middleware.db.router.dynamic;

import com.wang.middleware.db.router.DBContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @Author: whiteandbird
 * @Descripter:
 * @Date: 2022:07:17  21:28
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * 动态切换数据源
     * 每当切换数据源的时候 都要从这个里面进行获取
     * @return
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return "db" + DBContextHolder.getDBKey();
    }
}
