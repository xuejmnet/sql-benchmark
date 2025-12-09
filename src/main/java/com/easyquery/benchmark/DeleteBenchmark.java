package com.easyquery.benchmark;

import com.easyquery.benchmark.entity.User;
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
public class DeleteBenchmark {

    private DefaultEasyEntityQuery easyEntityQuery;
    private DSLContext jooqDsl;
    private EntityManager entityManager;

    @Setup(Level.Trial)
    public void setup() {
        DatabaseInitializer.getDataSource();

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
    }

    @Setup(Level.Iteration)
    public void setupIteration() {
        DatabaseInitializer.clearData();
        for (int i = 0; i < 50; i++) {
            String id = UUID.randomUUID().toString();
            DatabaseInitializer.insertUserWithJdbc(id, "user_" + i, "user" + i + "@example.com", 20 + i, "1234567890", "Address " + i);
        }
    }

    @Benchmark
    public long easyQueryDeleteByCondition() {
        try (Transaction transaction = easyEntityQuery.beginTransaction()) {
            long result = easyEntityQuery.deletable(User.class)
                    .allowDeleteStatement(true)
                    .where(u -> u.age().ge(40))
                    .executeRows();
            transaction.commit();
            return result;
        }
    }

    @Benchmark
    public int jooqDeleteByCondition() {
        return jooqDsl.transactionResult(configuration -> {
            return DSL.using(configuration)
                    .deleteFrom(T_USER)
                    .where(T_USER.AGE.ge(40))
                    .execute();
        });
    }

    @Benchmark
    public int hibernateDeleteByCondition() {
        entityManager.getTransaction().begin();
        try {
            Query query = entityManager.createQuery("DELETE FROM HibernateUser u WHERE u.age >= :minAge");
            query.setParameter("minAge", 40);
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
