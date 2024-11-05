package com.huazhao.mybatis.config;


import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.util.ObjectUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;


@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})

@Slf4j
public class SqlInterceptor implements Interceptor {
    // ANSI转义码
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_MAGENTA = "\u001B[35m";
    private static final String RED = "\u001B[31m";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object proceed = invocation.proceed();
        long endTime = System.currentTimeMillis();

        String printSql = null;
        try {
            printSql = generateSql(invocation);
        } catch (Exception exception) {
            log.error("获取sql异常", exception, RED);
        } finally {
            long costTime = endTime - startTime;
            log.info("\n 执行SQL耗时：{}ms \n 执行SQL：{}{}", costTime, ANSI_MAGENTA, printSql, ANSI_CYAN);
        }
        return proceed;
    }

    private static String generateSql(Invocation invocation) {
        MappedStatement statement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        Configuration configuration = statement.getConfiguration();
        BoundSql boundSql = statement.getBoundSql(parameter);
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> params = boundSql.getParameterMappings();
        String sql = boundSql.getSql();
        sql = sql.replaceAll("[\\s]+", " ");
        if (!ObjectUtils.isEmpty(params) && !ObjectUtils.isEmpty(parameterObject)) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();

            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(parameterObject)));
            } else {
                for (ParameterMapping param : params) {
                    String propertyName = param.getProperty();
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));

                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                    } else {
                        sql = sql.replaceFirst("\\?", "缺失");
                    }
                }
            }
        }
        return sql;
    }

    private static String getParameterValue(Object object) {
        String value = "";
        if (object instanceof String) {
            value = "'" + object.toString() + "'";
        } else if (object instanceof Date) {
            DateFormat format = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + format.format((Date) object) + "'";
        } else if (!ObjectUtils.isEmpty(object)) {
            value = object.toString();
        }
        return value;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以通过properties配置插件参数
    }
}
