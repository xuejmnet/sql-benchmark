package com.easyquery.benchmark;

import com.easyquery.benchmark.entity.User;
import com.easyquery.benchmark.jooq.generated.tables.pojos.TUser;
import com.easyquery.benchmark.jooq.generated.tables.records.TUserRecord;
import com.easyquery.benchmark.hibernate.HibernateUser;
import com.easyquery.benchmark.hibernate.HibernateUtil;
import com.easy.query.core.api.client.EasyQueryClient;
import com.easy.query.core.basic.jdbc.tx.Transaction;
import com.easy.query.core.bootstrapper.EasyQueryBootstrapper;
import com.easy.query.h2.config.H2DatabaseConfiguration;
import com.easy.query.api.proxy.client.DefaultEasyEntityQuery;
import jakarta.persistence.EntityManager;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
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
public class InsertBenchmark {

    private DefaultEasyEntityQuery easyEntityQuery;
    private DSLContext jooqDsl;
    private EntityManager entityManager;

    @Setup(Level.Trial)
    public void setup() {
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
    }

    @Benchmark
    public void easyQueryInsertSingle() {
        try (Transaction transaction = easyEntityQuery.beginTransaction()) {
            String id = UUID.randomUUID().toString();
            User user = new User(id, "user_" + id, "user@example.com", 25, "1234567890", "Test Address");
            easyEntityQuery.insertable(user).executeRows();
            transaction.commit();
        }
    }

    @Benchmark
    public void jooqInsertSingle() {
        String id = UUID.randomUUID().toString();
        
        jooqDsl.transaction(configuration -> {
            DSL.using(configuration)
                    .insertInto(T_USER)
                    .set(T_USER.ID, id)
                    .set(T_USER.USERNAME, "user_" + id)
                    .set(T_USER.EMAIL, "user@example.com")
                    .set(T_USER.AGE, 25)
                    .set(T_USER.PHONE, "1234567890")
                    .set(T_USER.ADDRESS, "Test Address")
                    .execute();
        });
    }


    @Benchmark
    public void easyQueryInsertBatch1000() {
        try (Transaction transaction = easyEntityQuery.beginTransaction()) {
            List<User> users = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                String id = UUID.randomUUID().toString();
                User user = new User(id, "user_" + id, "user@example.com", 25 + (i % 50), "1234567890", "Test Address");
                users.add(user);
            }
            easyEntityQuery.insertable(users).batch(true).executeRows();
            transaction.commit();
        }
    }


    @Benchmark
    public void jooqInsertBatch1000() {
        List<TUserRecord> records = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            String id = UUID.randomUUID().toString();
            TUserRecord record = new TUserRecord();
            record.setId(id);
            record.setUsername("user_" + id);
            record.setEmail("user@example.com");
            record.setAge(25 + (i % 50));
            record.setPhone("1234567890");
            record.setAddress("Test Address");
            records.add(record);
        }
        
        jooqDsl.transaction(configuration -> {
            DSL.using(configuration).batchInsert(records).execute();
        });
    }

    @Benchmark
    public void hibernateInsertSingle() {
        entityManager.getTransaction().begin();
        try {
            String id = UUID.randomUUID().toString();
            HibernateUser user = new HibernateUser(id, "user_" + id, "user@example.com", 25, "1234567890", "Test Address");
            entityManager.persist(user);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
    }

    @Benchmark
    public void hibernateInsertBatch1000() {
        entityManager.getTransaction().begin();
        try {
            for (int i = 0; i < 1000; i++) {
                String id = UUID.randomUUID().toString();
                HibernateUser user = new HibernateUser(id, "user_" + id, "user@example.com", 25 + (i % 50), "1234567890", "Test Address");
                entityManager.persist(user);
            }
            entityManager.flush();
            entityManager.clear();
            entityManager.getTransaction().commit();
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
    }
}
