package com.wang.middleware.db.router.annotation;


import java.lang.annotation.*;

/**
 * 路由
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouter {

    /**
     * 分库分表字段
     * 根据这个值来进行
     * @return
     */
    String key() default "";
}
