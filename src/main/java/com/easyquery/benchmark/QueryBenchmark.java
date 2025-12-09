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
    private String[] testUserIds;
    private int userIdIndex = 0;

    @Setup(Level.Trial)
    public void setup() {
        DatabaseInitializer.getDataSource();
        DatabaseInitializer.clearData();

        EasyQueryClient easyQueryClient = EasyQueryBootstrapper.defaultBuilderConfiguration()
                .setDefaultDataSource(DatabaseInitializer.getDataSource())
                .optionConfigure(op->{
                    op.setPrintSql(false);
                })
                .useDatabaseConfigure(new H2DatabaseConfiguration())
                .build();
        easyEntityQuery = new DefaultEasyEntityQuery(easyQueryClient);

        jooqDsl = DSL.using(DatabaseInitializer.getDataSource(), SQLDialect.H2);

        entityManager = HibernateUtil.createEntityManager();

        insertTestData();
    }

    @Setup(Level.Iteration)
    public void setupIteration() {
        userIdIndex = 0;
        // Clear Hibernate's first-level cache to ensure fair comparison
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.clear();
        }
    }

    private void insertTestData() {
        testUserIds = new String[100];
        for (int i = 0; i < 1000; i++) {
            String id = UUID.randomUUID().toString();
            if (i >= 400 && i < 500) {
                testUserIds[i - 400] = id;
            }
            DatabaseInitializer.insertUserWithJdbc(id, "user_" + i, "user" + i + "@example.com", 20 + (i % 50), "1234567890", "Address " + i);
        }
    }

    @Benchmark
    public User easyQuerySelectById() {
        String userId = testUserIds[(userIdIndex++) % testUserIds.length];
        return easyEntityQuery.queryable(User.class)
                .where(u -> u.id().eq(userId))
                .firstOrNull();
    }

    @Benchmark
    public TUser jooqSelectById() {
        String userId = testUserIds[(userIdIndex++) % testUserIds.length];
        return jooqDsl.selectFrom(T_USER)
                .where(T_USER.ID.eq(userId))
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
        String userId = testUserIds[(userIdIndex++) % testUserIds.length];
        // Use native query instead of find() to avoid first-level cache advantage
        return (HibernateUser) entityManager.createNativeQuery(
                "SELECT * FROM t_user WHERE id = ?", HibernateUser.class)
                .setParameter(1, userId)
                .getSingleResult();
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
