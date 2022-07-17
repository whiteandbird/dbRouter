package com.wang.middleware.db.router;

/**
 * @Author: whiteandbird
 * @Descripter:
 * @Date: 2022:07:17  18:57
 */
public class DBRouterBase {

    /**
     * 第几个表
     */
    private String tbIdx;

    public String getTbIdx(){
        return DBContextHolder.getTBKey();
    }
}
