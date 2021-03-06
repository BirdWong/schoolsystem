package cn.jsuacm.ccw.config;

import com.baomidou.mybatisplus.core.parser.ISqlParser;
import com.baomidou.mybatisplus.extension.parsers.BlockAttackSqlParser;
import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PerformanceInterceptor;
import com.baomidou.mybatisplus.extension.plugins.SqlExplainInterceptor;
import net.sf.jsqlparser.statement.delete.Delete;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName MybatisPlusConfig
 * @Description MybatisPlus 配置文件
 * @Author h4795
 * @Date 2019/06/19 21:01
 */

@EnableTransactionManagement
@Configuration
@MapperScan("cn.jsuacm.ccw.mapper")
public class MybatisPlusConfig {


    /**
     * 分页插件
     * @return
     */
    @Bean
    public PaginationInterceptor getPaginationInterceptor(){


        /*
         *
         * 执行拦截操作
         */
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();


        List<ISqlParser> sqlParserList = new ArrayList<>();
        // 攻击 SQL 阻断解析器、加入解析链
        sqlParserList.add(new BlockAttackSqlParser() {
            @Override
            public void processDelete(Delete delete) {
                super.processDelete(delete);
            }
        });
        paginationInterceptor.setSqlParserList(sqlParserList);

        return paginationInterceptor;

    }


    /**
     * 性能分析器
     * @return
     */
//    @Bean
//    public PerformanceInterceptor getPerformanceInterceptor(){
//        /*
//         * 参数：maxTime SQL 执行最大时长，超过自动停止运行，有助于发现问题。
//         * 参数：format SQL SQL是否格式化，默认false。
//         * 该插件只用于开发环境，不建议生产环境使用。
//         */
//        PerformanceInterceptor performanceInterceptor = new PerformanceInterceptor();
//        performanceInterceptor.setMaxTime(1000);
//        performanceInterceptor.setFormat(true);
//        return performanceInterceptor;
//    }

    /**
     *
     * 执行拦截操作
     *
     * *******与前面两个冲突，开发环境都用不了********
     * 自己多注意
     * 报错： There is no getter for property named 'delegate' in 'class com.sun.prox
     */
//    @Bean
//    public SqlExplainInterceptor sqlExplainInterceptor(){
//
//        SqlExplainInterceptor sqlExplainInterceptor = new SqlExplainInterceptor();
//        List<ISqlParser> sqlParserList = new ArrayList<>();
//        sqlParserList.add(new BlockAttackSqlParser());
//        sqlExplainInterceptor.setSqlParserList(sqlParserList);
//        return sqlExplainInterceptor;
//    }


    /**
     * 乐观锁配置，实体必须有version对应的属性， 并且添加version注解
     * @return
     */
    @Bean
    public OptimisticLockerInterceptor optimisticLockerInterceptor(){
        return new OptimisticLockerInterceptor();
    }
}
