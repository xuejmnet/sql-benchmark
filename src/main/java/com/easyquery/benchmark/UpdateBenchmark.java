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
public class UpdateBenchmark {

    private DefaultEasyEntityQuery easyEntityQuery;
    private DSLContext jooqDsl;
    private String testUserId;

    @Setup(Level.Trial)
    public void setup() {
        DatabaseInitializer.getDataSource();
        DatabaseInitializer.clearData();

        // 初始化 easy-query
        EasyQueryClient easyQueryClient = EasyQueryBootstrapper.defaultBuilderConfiguration()
                .setDefaultDataSource(DatabaseInitializer.getDataSource())
                .useDatabaseConfigure(new H2DatabaseConfiguration())
                .build();
        easyEntityQuery = new DefaultEasyEntityQuery(easyQueryClient);

        // 初始化 JOOQ
        jooqDsl = DSL.using(DatabaseInitializer.getDataSource(), SQLDialect.H2);

        // 插入测试数据
        insertTestData();
    }

    private void insertTestData() {
        for (int i = 0; i < 100; i++) {
            String id = UUID.randomUUID().toString();
            if (i == 50) {
                testUserId = id;
            }
            DatabaseInitializer.insertUserWithJdbc(id, "user_" + i, "user" + i + "@example.com", 20 + i, "1234567890", "Address " + i);
        }
    }

    @Benchmark
    public long easyQueryUpdateById() {
        return easyEntityQuery.updatable(User.class)
                .setColumns(u -> {
                    u.age().set(99);
                })
                .where(u -> u.id().eq(testUserId))
                .executeRows();
    }

    @Benchmark
    public int jooqUpdateById() {
        return jooqDsl.update(table("t_user"))
                .set(field("age"), 99)
                .where(field("id").eq(testUserId))
                .execute();
    }

    @Benchmark
    public long easyQueryUpdateBatch() {
        return easyEntityQuery.updatable(User.class)
                .setColumns(u -> {
                    u.age().set(88);
                })
                .where(u -> u.age().ge(50))
                .executeRows();
    }

    @Benchmark
    public int jooqUpdateBatch() {
        return jooqDsl.update(table("t_user"))
                .set(field("age"), 88)
                .where(field("age").ge(50))
                .execute();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        DatabaseInitializer.clearData();
    }
}
