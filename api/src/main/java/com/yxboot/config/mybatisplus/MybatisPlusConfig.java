package com.yxboot.config.mybatisplus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

/**
 * MybatisPLus 配置
 * 
 * @author Boya
 */
@Configuration
@MapperScan(basePackages = "com.yxboot.modules.*.mapper")
public class MybatisPlusConfig {
    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * SQL 打印配置
     * 设置 MyBatis-Plus 打印 SQL 语句
     */
    // @Bean
    // @Profile({ "dev", "test" }) // 仅在开发和测试环境启用
    // public org.apache.ibatis.session.Configuration mybatisPlusConfiguration() {
    // org.apache.ibatis.session.Configuration configuration = new
    // org.apache.ibatis.session.Configuration();
    // configuration.setLogImpl(StdOutImpl.class);
    // return configuration;
    // }
}
