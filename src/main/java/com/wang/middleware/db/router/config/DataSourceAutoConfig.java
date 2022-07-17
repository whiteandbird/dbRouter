package com.wang.middleware.db.router.config;

import com.wang.middleware.db.router.DBRouterConfig;
import com.wang.middleware.db.router.DBRouterJoinPoint;
import com.wang.middleware.db.router.dynamic.DynamicDataSource;
import com.wang.middleware.db.router.dynamic.DynamicMybatisPlugin;
import com.wang.middleware.db.router.strategy.IDBRouterStrategy;
import com.wang.middleware.db.router.strategy.impl.DBRouterStrategyHashCode;
import com.wang.middleware.db.router.util.PropertyUtil;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: whiteandbird
 * @Descripter:
 * @Date: 2022:07:17  17:31
 */
@Configuration
public class DataSourceAutoConfig implements EnvironmentAware {


    /**
     * 数据源配置组
     */
    private Map<String, Map<String, Object>> dataSourcesMap = new HashMap<>();

    /**
     * 默认数据源配置
     */
    private Map<String, Object> defaultDataSourceConfig;

    /**
     * 分库数量
     */
    private int dbCount;

    /**
     * 分表数量
     */
    private int tbCount;

    /**
     * 路由字段
     */
    private String routerKey;

    @Override
    public void setEnvironment(Environment environment) {
        // 进行数据源的解析
        // 前缀 从配置文件中读取
        String prefix = "mini-db-router.jdbc.datasource.";

        dbCount = Integer.parseInt(environment.getProperty(prefix + "dbCount"));
        tbCount = Integer.parseInt(environment.getProperty(prefix + "tbCount"));

        // 获取路由key
        routerKey = environment.getProperty(prefix+"routerKey");

        // 分库分表数据源
        String dataSources = environment.getProperty(prefix + "list");
        assert  dataSources != null;
        for(String dbInfo : dataSources.split(",")){
            Map<String, Object> dataSourceMap= PropertyUtil.handle(environment, prefix + dbInfo, Map.class);
            dataSourcesMap.put(dbInfo, dataSourceMap);
        }

        String defaultDb = environment.getProperty(prefix + "default");
        defaultDataSourceConfig = PropertyUtil.handle(environment, prefix+ defaultDb, Map.class);

    }

    @Bean(name = "db-router-point")
    @ConditionalOnMissingBean
    public DBRouterJoinPoint dbRouterJoinPoint(DBRouterConfig dbRouterConfig, IDBRouterStrategy dbRouterStrategy){
        return new DBRouterJoinPoint(dbRouterConfig, dbRouterStrategy);
    }

    @Bean
    public DBRouterConfig dbRouterConfig(){
        return new DBRouterConfig(dbCount, tbCount, routerKey);
    }

    @Bean
    public IDBRouterStrategy idbRouterStrategy(DBRouterConfig dbRouterConfig){
        return new DBRouterStrategyHashCode(dbRouterConfig);
    }


    /**
     * 注入mybatis插件
     */
    @Bean
    public Interceptor dbrouterPlugin(){
        return new DynamicMybatisPlugin();
    }

    /**
     * 数据源从创建处理
     */
    @Bean
    public DataSource dataSource(){
        Map<Object, Object> targetDataSources = new HashMap<>();
        for(String dbInfo : dataSourcesMap.keySet()){
            // 每个数据库的配置
            Map<String, Object> objMap = dataSourcesMap.get(dbInfo);
            // 读取配置创建管理
            targetDataSources.put(dbInfo, new DriverManagerDataSource(objMap.get("url").toString(), objMap.get("username").toString(), objMap.get("password").toString()));
        }

        // 继承了spring 封装的动态数据源处理 并且重写了拿库的操作
        DynamicDataSource dynamicDataSource = new DynamicDataSource();

        // 多数据库源
        dynamicDataSource.setTargetDataSources(targetDataSources);

        // 设置一个默认的数据源
        dynamicDataSource.setDefaultTargetDataSource(new DriverManagerDataSource(defaultDataSourceConfig.get("url").toString(), defaultDataSourceConfig.get("username").toString(), defaultDataSourceConfig.get("password").toString()));

        return dynamicDataSource;
     }

    /**
     * 事务管理
     * @param dataSource
     * @return
     */
     @Bean
     public TransactionTemplate transactionTemplate(DataSource dataSource){
         // 管理的上面那个被注入的
         DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
         dataSourceTransactionManager.setDataSource(dataSource);

         TransactionTemplate transactionTemplate = new TransactionTemplate();
         transactionTemplate.setTransactionManager(dataSourceTransactionManager);
         // 传播行为
         transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRED");
         return transactionTemplate;

     }
}
