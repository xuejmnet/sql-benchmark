package com.easyquery.benchmark;

import com.easyquery.benchmark.entity.User;
import com.easyquery.benchmark.hibernate.HibernateUser;
import com.easyquery.benchmark.hibernate.HibernateUtil;
import com.easy.query.api.proxy.client.DefaultEasyEntityQuery;
import com.easy.query.core.api.client.EasyQueryClient;
import com.easy.query.core.basic.jdbc.tx.Transaction;
import com.easy.query.core.bootstrapper.EasyQueryBootstrapper;
import com.easy.query.h2.config.H2DatabaseConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.openjdk.jmh.annotations.*;

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
public class UpdateBenchmark {

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
    }

    private void insertTestData() {
        testUserIds = new String[50];
        for (int i = 0; i < 100; i++) {
            String id = UUID.randomUUID().toString();
            if (i >= 25 && i < 75) {
                testUserIds[i - 25] = id;
            }
            DatabaseInitializer.insertUserWithJdbc(id, "user_" + i, "user" + i + "@example.com", 20 + i, "1234567890", "Address " + i);
        }
    }

    @Benchmark
    public long easyQueryUpdateById() {
        String userId = testUserIds[(userIdIndex++) % testUserIds.length];
        try (Transaction transaction = easyEntityQuery.beginTransaction()) {
            long result = easyEntityQuery.updatable(User.class)
                    .setColumns(u -> {
                        u.age().set(99);
                    })
                    .where(u -> u.id().eq(userId))
                    .executeRows();
            transaction.commit();
            return result;
        }
    }

    @Benchmark
    public int jooqUpdateById() {
        String userId = testUserIds[(userIdIndex++) % testUserIds.length];
        return jooqDsl.transactionResult(configuration -> {
            return DSL.using(configuration)
                    .update(T_USER)
                    .set(T_USER.AGE, 99)
                    .where(T_USER.ID.eq(userId))
                    .execute();
        });
    }

    @Benchmark
    public long easyQueryUpdateBatch() {
        try (Transaction transaction = easyEntityQuery.beginTransaction()) {
            long result = easyEntityQuery.updatable(User.class)
                    .setColumns(u -> {
                        u.age().set(88);
                    })
                    .where(u -> u.age().ge(50))
                    .executeRows();
            transaction.commit();
            return result;
        }
    }

    @Benchmark
    public int jooqUpdateBatch() {
        return jooqDsl.transactionResult(configuration -> {
            return DSL.using(configuration)
                    .update(T_USER)
                    .set(T_USER.AGE, 88)
                    .where(T_USER.AGE.ge(50))
                    .execute();
        });
    }

    @Benchmark
    public int hibernateUpdateById() {
        String userId = testUserIds[(userIdIndex++) % testUserIds.length];
        entityManager.getTransaction().begin();
        try {
            Query query = entityManager.createQuery("UPDATE HibernateUser u SET u.age = :age WHERE u.id = :id");
            query.setParameter("age", 99);
            query.setParameter("id", userId);
            int result = query.executeUpdate();
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    @Benchmark
    public int hibernateUpdateBatch() {
        entityManager.getTransaction().begin();
        try {
            Query query = entityManager.createQuery("UPDATE HibernateUser u SET u.age = :age WHERE u.age >= :minAge");
            query.setParameter("age", 88);
            query.setParameter("minAge", 50);
            int result = query.executeUpdate();
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
        DatabaseInitializer.clearData();
    }
}
