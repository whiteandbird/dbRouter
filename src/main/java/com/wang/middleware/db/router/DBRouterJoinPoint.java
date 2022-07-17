package com.wang.middleware.db.router;

import com.wang.middleware.db.router.annotation.DBRouter;
import com.wang.middleware.db.router.strategy.IDBRouterStrategy;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @Author: whiteandbird
 * @Descripter:
 * @Date: 2022:07:17  18:56
 */
@Aspect
public class DBRouterJoinPoint {

    private Logger logger = LoggerFactory.getLogger(DBRouterJoinPoint.class);


    private DBRouterConfig dbRouterConfig;


    private IDBRouterStrategy idbRouterStrategy;

    public DBRouterJoinPoint(DBRouterConfig dbRouterConfig, IDBRouterStrategy strategy){
        this.dbRouterConfig = dbRouterConfig;
        this.idbRouterStrategy = strategy;
    }

    @Pointcut("@annotation(com.wang.middleware.db.router.annotation.DBRouter)")
    public void aopPoint(){

    }

    /**
     * 所有需要分库分表的操作，都需要使用自定义注解进行拦截，拦截后读取方法中的入参字段，根据字段进行路由操作。
     * 1. dbRouter.key() 确定根据哪个字段进行路由
     * 2. getAttrValue 根据数据库路由字段，从入参中读取出对应的值。比如路由 key 是 uId，那么就从入参对象 Obj 中获取到 uId 的值。
     * 3. dbRouterStrategy.doRouter(dbKeyAttr) 路由策略根据具体的路由值进行处理
     * 4. 路由处理完成比，就是放行。 jp.proceed();
     * 5. 最后 dbRouterStrategy 需要执行 clear 因为这里用到了 ThreadLocal 需要手动清空。关于 ThreadLocal 内存泄漏介绍 https://t.zsxq.com/027QF2fae
     */
    @Around("aopPoint() && @annotation(dbRouter)")
    public Object doRouter(ProceedingJoinPoint jp, DBRouter dbRouter) throws Throwable{
        String dbKey = dbRouter.key();
        if(StringUtils.isBlank(dbKey) && StringUtils.isBlank(dbRouterConfig.getRouterKey())){
            throw new RuntimeException("annotation router key is null!");
        }

        // key 的优先级关系
        dbKey = StringUtils.isNotBlank(dbKey) ? dbKey : dbRouterConfig.getRouterKey();


        String dbKeyAttr = getAttrValue(dbKey, jp.getArgs());

        // 设置 选择哪个表以及哪个库
        // 将其设置到线程上下文中
        idbRouterStrategy.doRouter(dbKeyAttr);
        try{
            // 方法执行的时候才会取得对应的表以及库进行动态路由
            return jp.proceed();
        }finally {
            // 删除上下文数据
            idbRouterStrategy.clear();
        }
    }

    public String getAttrValue(String attr, Object[] args){
        if(1 == args.length){
            Object arg = args[0];
            if(arg instanceof String){
                return arg.toString();
            }
        }

        String fieldValue = null;
        for(Object arg : args){
            try{
                if(StringUtils.isNotBlank(fieldValue)){
                    break;
                }
                fieldValue = BeanUtils.getProperty(arg, attr);
            }catch (Exception e){
                logger.error("获取路由属性失败 attr: {}", attr, e);
            }
        }

        return fieldValue;
    }

}
