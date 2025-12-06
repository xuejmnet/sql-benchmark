package com.easyquery.benchmark;

import com.easyquery.benchmark.entity.User;
import com.easy.query.core.api.client.EasyQueryClient;
import com.easy.query.core.bootstrapper.EasyQueryBootstrapper;
import com.easy.query.h2.config.H2DatabaseConfiguration;
import com.easy.query.api.proxy.client.DefaultEasyEntityQuery;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
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
public class InsertBenchmark {

    private DefaultEasyEntityQuery easyEntityQuery;
    private DSLContext jooqDsl;

    @Setup(Level.Trial)
    public void setup() {
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
    }

    @Benchmark
    public void easyQueryInsertSingle() {
        String id = UUID.randomUUID().toString();
        User user = new User(id, "user_" + id, "user@example.com", 25, "1234567890", "Test Address");
        easyEntityQuery.insertable(user).executeRows();
    }

    @Benchmark
    public void jooqInsertSingle() {
        String id = UUID.randomUUID().toString();
        com.easyquery.benchmark.jooq.JooqUser user = new com.easyquery.benchmark.jooq.JooqUser(
                id, "user_" + id, "user@example.com", 25, "1234567890", "Test Address");
        
        jooqDsl.insertInto(table("t_user"))
                .columns(
                        field("id"), field("username"), field("email"), 
                        field("age"), field("phone"), field("address")
                )
                .values(user.getId(), user.getUsername(), user.getEmail(), 
                        user.getAge(), user.getPhone(), user.getAddress())
                .execute();
    }


    @Benchmark
    public void easyQueryInsertBatch10() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String id = UUID.randomUUID().toString();
            User user = new User(id, "user_" + id, "user@example.com", 25 + i, "1234567890", "Test Address");
            users.add(user);
        }
        easyEntityQuery.insertable(users).executeRows();
    }


    @Benchmark
    public void jooqInsertBatch10() {
        List<com.easyquery.benchmark.jooq.JooqUser> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String id = UUID.randomUUID().toString();
            com.easyquery.benchmark.jooq.JooqUser user = new com.easyquery.benchmark.jooq.JooqUser(
                    id, "user_" + id, "user@example.com", 25 + i, "1234567890", "Test Address");
            users.add(user);
        }
        
        var batchQuery = jooqDsl.batch(
                jooqDsl.insertInto(table("t_user"))
                        .columns(
                                field("id"), field("username"), field("email"),
                                field("age"), field("phone"), field("address")
                        )
                        .values((String) null, null, null, null, null, null)
        );
        
        for (com.easyquery.benchmark.jooq.JooqUser user : users) {
            batchQuery.bind(
                    user.getId(), user.getUsername(), user.getEmail(),
                    user.getAge(), user.getPhone(), user.getAddress()
            );
        }
        
        batchQuery.execute();
    }

    @TearDown(Level.Trial)
    public void tearDown() {

    }
}
