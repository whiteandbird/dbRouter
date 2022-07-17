package com.wang.middleware.db.router.dynamic;

import com.wang.middleware.db.router.DBContextHolder;
import com.wang.middleware.db.router.annotation.DBRouterStrategy;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * mybatis 拦截器   通过对sql语句的拦截处理 修改分表信息
 * @Author: whiteandbird
 * @Descripter:
 * @Date: 2022:07:17  21:30
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args={Connection.class, Integer.class})})
public class DynamicMybatisPlugin implements Interceptor {

    private Pattern pattern = Pattern.compile("(from|into|update)[\\s]{1,}(\\w{1,})", Pattern.CASE_INSENSITIVE);
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler handler = (StatementHandler)invocation.getTarget();
        MetaObject metaObject = MetaObject.forObject(handler, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

        //
        String id = mappedStatement.getId();
        // id 是精确到了方法的 是mapper中每个语句的id  对应了方法 所以截断就能拿到对应的类
        String className = id.substring(0, id.lastIndexOf("."));
        Class<?> clazz = Class.forName(className);
        DBRouterStrategy dbRouterStrategy = clazz.getAnnotation(DBRouterStrategy.class);

        // 没有注解或者不进行分表处理
        if(null == dbRouterStrategy || ! dbRouterStrategy.splitTable()){
            return  invocation.proceed();
        }

        // 接下来需要处理进行分表的操作
        // 获取到对应的sql
        BoundSql boundSql = handler.getBoundSql();
        String sql = boundSql.getSql();

        // 因为这部分是要进行分分表操作 所以需要更改对应的表名
        Matcher matcher = pattern.matcher(sql);
        String tableName = null;
        if(matcher.find()){
            tableName = matcher.group().trim();
        }
        assert  null != tableName;
        String replaceSql =  matcher.replaceAll(tableName+"_"+ DBContextHolder.getTBKey());
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, replaceSql);
        field.setAccessible(false);

        return invocation.proceed();

    }
}
