package com.wang.middleware.db.router.annotation;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouterStrategy {

    /**
     * 所以所能进行分库  但不一定进行分表
     * 是否分表
     * @return
     */
    boolean splitTable() default false;
}
