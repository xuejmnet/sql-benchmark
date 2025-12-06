package com.easyquery.benchmark;

import com.easyquery.benchmark.entity.User;
import com.easyquery.benchmark.jooq.JooqUser;
import com.easy.query.api.proxy.client.DefaultEasyEntityQuery;
import com.easy.query.core.api.client.EasyQueryClient;
import com.easy.query.core.bootstrapper.EasyQueryBootstrapper;
import com.easy.query.h2.config.H2DatabaseConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.openjdk.jmh.annotations.*;

import java.util.List;
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
public class QueryBenchmark {

    private DefaultEasyEntityQuery easyEntityQuery;
    private DSLContext jooqDsl;
    private String testUserId;

    @Setup(Level.Trial)
    public void setup() {
        // 初始化数据库
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
        for (int i = 0; i < 1000; i++) {
            String id = UUID.randomUUID().toString();
            if (i == 500) {
                testUserId = id;
            }
            DatabaseInitializer.insertUserWithJdbc(id, "user_" + i, "user" + i + "@example.com", 20 + (i % 50), "1234567890", "Address " + i);
        }
    }

    @Benchmark
    public User easyQuerySelectById() {
        return easyEntityQuery.queryable(User.class)
                .where(u -> u.id().eq(testUserId))
                .firstOrNull();
    }

    @Benchmark
    public JooqUser jooqSelectById() {
        return jooqDsl.select()
                .from(table("t_user"))
                .where(field("id").eq(testUserId))
                .fetchOneInto(JooqUser.class);
    }

    @Benchmark
    public List<User> easyQuerySelectList() {
        return easyEntityQuery.queryable(User.class)
                .where(u -> u.age().ge(25))
                .orderBy(u -> u.username().desc())
                .limit(10)
                .toList();
    }

    @Benchmark
    public List<JooqUser> jooqSelectList() {
        return jooqDsl.select()
                .from(table("t_user"))
                .where(field("age").ge(25))
                .orderBy(field("username").desc())
                .limit(10)
                .fetchInto(JooqUser.class);
    }

    @Benchmark
    public long easyQueryCount() {
        return easyEntityQuery.queryable(User.class)
                .where(u -> {
                    u.age().ge(25);
                    u.age().le(35);
                })
                .count();
    }

    @Benchmark
    public long jooqCount() {
        Integer count = jooqDsl.selectCount()
                .from(table("t_user"))
                .where(field("age").ge(25).and(field("age").le(35)))
                .fetchOne(0, Integer.class);
        return count != null ? count : 0;
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        DatabaseInitializer.clearData();
    }
}
