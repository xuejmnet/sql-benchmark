package com.easyquery.benchmark;

import com.easyquery.benchmark.entity.User;
import com.easy.query.api.proxy.client.DefaultEasyEntityQuery;
import com.easy.query.core.api.client.EasyQueryClient;
import com.easy.query.core.bootstrapper.EasyQueryBootstrapper;
import com.easy.query.h2.config.H2DatabaseConfiguration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.openjdk.jmh.annotations.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.jooq.impl.DSL.*;


@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@Fork(1)
@Threads(1)
public class DeleteBenchmark {

    private DefaultEasyEntityQuery easyEntityQuery;
    private DSLContext jooqDsl;

    @Setup(Level.Trial)
    public void setup() {
        DatabaseInitializer.getDataSource();

        // 初始化 easy-query
        EasyQueryClient easyQueryClient = EasyQueryBootstrapper.defaultBuilderConfiguration()
                .setDefaultDataSource(DatabaseInitializer.getDataSource())
                .useDatabaseConfigure(new H2DatabaseConfiguration())
                .build();
        easyEntityQuery = new DefaultEasyEntityQuery(easyQueryClient);

        // 初始化 JOOQ
        jooqDsl = DSL.using(DatabaseInitializer.getDataSource(), SQLDialect.H2);
    }

    @Setup(Level.Iteration)
    public void setupIteration() {
        DatabaseInitializer.clearData();
        // 使用中立的 JDBC 方式插入新数据（确保公平性）
        for (int i = 0; i < 50; i++) {
            String id = UUID.randomUUID().toString();
            DatabaseInitializer.insertUserWithJdbc(id, "user_" + i, "user" + i + "@example.com", 20 + i, "1234567890", "Address " + i);
        }
    }

    @Benchmark
    public long easyQueryDeleteByCondition() {
        return easyEntityQuery.deletable(User.class)
                .allowDeleteStatement(true)
                .where(u -> u.age().ge(40))
                .executeRows();
    }

    @Benchmark
    public int jooqDeleteByCondition() {
        return jooqDsl.deleteFrom(table("t_user"))
                .where(field("age").ge(40))
                .execute();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        DatabaseInitializer.clearData();
    }
}
