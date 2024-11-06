package com.huazhao.mybatis.config;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
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

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;


@Intercepts({@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}), @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}), @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})

@Slf4j
public class SqlInterceptor implements Interceptor {
    // ANSI转义码
    private static final String ANSI_RESET = "\033[0m";
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
            log.warn("无法解析SQL，可能是由于动态参数问题。此日志信息并不代表执行SQL存在问题。具体异常信息：{}{}{}", RED, exception.getMessage(), ANSI_RESET);
        } finally {
            long costTime = endTime - startTime;
            log.info("\n 执行SQL耗时：{}ms \n 执行SQL：{}{}{}", costTime, ANSI_MAGENTA, printSql, ANSI_RESET);
        }
        return proceed;
    }

    private static String generateSql(Invocation invocation) {
        MappedStatement statement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = (invocation.getArgs().length > 1) ? invocation.getArgs()[1] : null;
        Configuration configuration = statement.getConfiguration();
        BoundSql boundSql = statement.getBoundSql(parameter);

        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> params = boundSql.getParameterMappings();
        String sql = boundSql.getSql();

        CacheKey cacheKey = (CacheKey) invocation.getArgs()[4];
        List<Object> updateList = getUpdateListFromCacheKey(cacheKey);
        if (ObjectUtils.isNotEmpty(updateList)) {
            String str = (String) updateList.get(3);
            if (str.length() < 12) {
                String[] split = str.split(":");
                sql += " LIMIT " + Integer.parseInt(split[0]) + ", " + Integer.parseInt(split[1]);
            }
        }
        sql = sql.replaceAll("[\\s]+", " ");
        if (!ObjectUtils.isEmpty(params) && parameterObject != null) {
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
                        sql = sql.replaceFirst("\\?", "未知参数");
                    }
                }
            }
        }
        return sql;
    }

    private static List<Object> getUpdateListFromCacheKey(CacheKey cacheKey) {
        try {
            Field field = CacheKey.class.getDeclaredField("updateList");
            field.setAccessible(true);
            return (List<Object>) field.get(cacheKey);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    private static String getParameterValue(Object obj) {
        if (obj instanceof String) {
            return "'" + obj + "'";
        } else if (obj instanceof Date) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            return "'" + df.format((Date) obj) + "'";
        } else if (obj != null) {
            return obj.toString();
        }
        return "";
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
}
