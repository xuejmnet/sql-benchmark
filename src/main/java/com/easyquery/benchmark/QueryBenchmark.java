package com.easyquery.benchmark;

import com.easyquery.benchmark.entity.User;
import com.easyquery.benchmark.jooq.generated.tables.pojos.TUser;
import com.easyquery.benchmark.hibernate.HibernateUser;
import com.easyquery.benchmark.hibernate.HibernateUtil;
import com.easy.query.api.proxy.client.DefaultEasyEntityQuery;
import com.easy.query.core.api.client.EasyQueryClient;
import com.easy.query.core.bootstrapper.EasyQueryBootstrapper;
import com.easy.query.h2.config.H2DatabaseConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.easyquery.benchmark.jooq.generated.Tables.T_USER;


@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 3)
@Measurement(iterations = 10, time = 3)
@Fork(3)
@Threads(1)
public class QueryBenchmark {

    private DefaultEasyEntityQuery easyEntityQuery;
    private DSLContext jooqDsl;
    private EntityManager entityManager;
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

        // 初始化 Hibernate
        entityManager = HibernateUtil.createEntityManager();

        // 插入测试数据
        insertTestData();
    }

    @Setup(Level.Iteration)
    public void setupIteration() {
        // 清理 Hibernate 一级缓存，避免缓存累积影响查询性能
        if (entityManager != null) {
            entityManager.clear();
        }
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
    public TUser jooqSelectById() {
        return jooqDsl.selectFrom(T_USER)
                .where(T_USER.ID.eq(testUserId))
                .fetchOneInto(TUser.class);
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
    public List<TUser> jooqSelectList() {
        return jooqDsl.selectFrom(T_USER)
                .where(T_USER.AGE.ge(25))
                .orderBy(T_USER.USERNAME.desc())
                .limit(10)
                .fetchInto(TUser.class);
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
    public Integer jooqCount() {
        return jooqDsl.selectCount()
                .from(T_USER)
                .where(T_USER.AGE.ge(25).and(T_USER.AGE.le(35)))
                .fetchOne(0, Integer.class);
    }

    @Benchmark
    public HibernateUser hibernateSelectById() {
        return entityManager.find(HibernateUser.class, testUserId);
    }

    @Benchmark
    public List<HibernateUser> hibernateSelectList() {
        TypedQuery<HibernateUser> query = entityManager.createQuery(
                "SELECT u FROM HibernateUser u WHERE u.age >= :age ORDER BY u.username DESC",
                HibernateUser.class);
        query.setParameter("age", 25);
        query.setMaxResults(10);
        return query.getResultList();
    }

    @Benchmark
    public long hibernateCount() {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(u) FROM HibernateUser u WHERE u.age >= :minAge AND u.age <= :maxAge",
                Long.class);
        query.setParameter("minAge", 25);
        query.setParameter("maxAge", 35);
        return query.getSingleResult();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
        DatabaseInitializer.clearData();
    }
}
