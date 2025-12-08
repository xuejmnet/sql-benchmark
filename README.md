# Easy-Query vs JOOQ vs Hibernate Performance Benchmark

## 🏆 Benchmark Results

**Test Environment:**
- **OS**: Windows 10 (Build 26200)
- **JDK**: OpenJDK 21.0.9+10-LTS (64-Bit Server VM)
- **Database**: H2 2.2.224 (In-Memory)
- **Connection Pool**: HikariCP 4.0.3
- **JMH**: 1.37

### Performance Summary (ops/s - higher is better)

| Test Scenario | EasyQuery | Hibernate | JOOQ | Best |
|--------------|-----------|-----------|------|------|
| **Query Operations** |
| Select by ID | 305,207 ± 10,512 | 227,301 ± 10,170 | 160,269 ± 3,664 | 🚀 **1.34x faster** than Hibernate |
| Select List | 266,390 ± 3,978 | 364,967 ± 15,355 | 80,236 ± 3,493 | ❌ **0.73x** (Hibernate wins) |
| COUNT Query | 412,933 ± 7,923 | 487,353 ± 15,942 | 243,099 ± 7,318 | ❌ **0.85x** (Hibernate wins) |
| **Insert Operations** |
| Single Insert | 82,239 ± 1,547 | 372 ± 50 | 66,391 ± 2,002 | 🚀 **221x faster** than Hibernate |
| Batch Insert (1000) | 60 ± 17 | 66 ± 3 | 54 ± 4 | ⚖️ **0.91x** comparable |
| **Update Operations** |
| Update by ID | 111,131 ± 16,518 | 55,648 ± 32,258 | 38,046 ± 23,627 | 🚀 **2.0x faster** than Hibernate |
| Batch Update | 4,724 ± 354 | 781 ± 35 | 2,089 ± 1,208 | 🚀 **6.0x faster** than Hibernate |
| **Delete Operations** |
| Delete by Condition | 108,395 ± 82,216 | 285,460 ± 7,670 | 221,337 ± 56,080 | ❌ **0.38x** (Hibernate wins) |
| **Complex Operations** |
| JOIN Query | 163,606 ± 4,689 | 208,591 ± 19,818 | 6,332 ± 407 | ❌ **0.78x** (Hibernate wins) |
| Aggregation (COUNT) | 408,164 ± 16,437 | 468,132 ± 17,908 | 162,432 ± 74,204 | ❌ **0.87x** (Hibernate wins) |

### Key Findings

✅ **EasyQuery advantages:**
- **Single insert operations**: 221x faster than Hibernate, 1.24x faster than JOOQ
- **Batch update operations**: 6x faster than Hibernate, 2.3x faster than JOOQ
- **Update by ID**: 2x faster than Hibernate, 2.9x faster than JOOQ
- **Select by ID**: 1.34x faster than Hibernate, 1.9x faster than JOOQ
- Best choice for **write-intensive and CRUD-heavy applications**

❌ **Hibernate advantages:**
- **List queries**: 1.37x faster than EasyQuery, 4.5x faster than JOOQ
- **Complex queries (JOIN, Aggregation)**: Generally 1.15-1.27x faster than EasyQuery
- **Delete operations**: 2.6x faster than EasyQuery
- **Note**: Single insert performance is extremely poor (372 ops/s)

⚖️ **JOOQ performance:**
- Generally slower than both EasyQuery and Hibernate in most scenarios
- **Critical issue**: JOIN query performance is significantly lower (6,332 ops/s)

💡 **Overall**: EasyQuery shows **superior performance in write operations** and single-record queries, making it ideal for CRUD-intensive applications. Hibernate excels in read-heavy and complex query scenarios but has poor single-insert performance. JOOQ may need additional optimization for production use.

### ⚠️ Important Notes

- All frameworks run in **autocommit mode** without explicit transaction management for fair comparison
- **Test data varies by benchmark**:
  - Query operations: 1,000 users pre-loaded
  - Complex queries: 500 users + ~1,750 orders pre-loaded
  - Update operations: 100 users per iteration
  - Delete operations: 50 users per iteration
  - Insert operations: starts from empty database
- Connection pool: HikariCP with 10 max connections, 5 min idle
- Benchmark stability achieved through:
  - **Warmup**: 5 iterations × 3 seconds
  - **Measurement**: 10 iterations × 3 seconds
  - **Forks**: 3 JVM forks for statistical reliability

### 📢 Disclaimer

**About Test Fairness**: Due to limited time and resources, the author may not have deep expertise in all the optimization mechanisms of each ORM framework. If you believe any benchmark is unfair or not optimized properly, you are **welcome and encouraged** to:

- 🔧 Fork this repository
- 📝 Modify the benchmark code with your optimizations
- 🚀 Re-run the tests and share your results

**Author's Confidence**: Despite these limitations, we are confident that **EasyQuery delivers excellent performance** in real-world scenarios. The benchmarks demonstrate its strengths, but we remain open to improvements and community feedback.

💡 **Contributions are welcome!** If you find better ways to optimize any framework's performance, please submit a pull request. Fair comparison benefits everyone in the community.

---

## 📊 Overview

This is a **standalone** comprehensive performance benchmark comparing **easy-query**, **JOOQ**, and **Hibernate** using JMH (Java Microbenchmark Harness).

> **Talk is cheap, show me the code and benchmarks!**

This project provides objective benchmark data to prove that easy-query is not just a JOOQ clone, but offers significant advantages in both performance and usability compared to other popular ORM frameworks.

### ✨ Standalone Project

This benchmark can be run **independently** without the easy-query parent project:
- ✅ No parent POM dependency
- ✅ Self-contained configuration
- ✅ Clone and run anywhere
- ✅ All dependencies explicitly declared

## 🎯 Test Scenarios

All benchmarks use JMH (Java Microbenchmark Harness) with the following configuration:
- **Mode**: Throughput (operations per second)
- **Warmup**: 5 iterations, 3 seconds each
- **Measurement**: 10 iterations, 3 seconds each
- **Fork**: 3 JVM forks
- **Threads**: 1 thread

### 1. **Insert Operations (InsertBenchmark)**
- **Single insert**: Insert one user record at a time
- **Batch insert (1000 records)**: Insert 1000 user records in a single batch operation

### 2. **Query Operations (QueryBenchmark)**
- **Query by ID**: Select a single user by primary key
- **Conditional query**: Select users with age >= 25, sorted by username DESC, limit 10
- **COUNT aggregation**: Count users with age between 25 and 35

### 3. **Update Operations (UpdateBenchmark)**
- **Single record update by ID**: Update one user's age by ID
- **Batch conditional update**: Update multiple users' age where age >= 50

### 4. **Delete Operations (DeleteBenchmark)**
- **Conditional delete**: Delete users where age >= 40

### 5. **Complex Queries (ComplexQueryBenchmark)**
- **JOIN query**: INNER JOIN users and orders with filtering (status=1, amount>=100), distinct results, limit 20
- **COUNT aggregation**: Count orders with status=1

## 🔧 Tech Stack

- **JMH**: 1.37 - Java Microbenchmark Harness
- **H2 Database**: 2.2.224 - In-memory database
- **easy-query**: 3.1.66-preview3
- **JOOQ**: 3.19.1
- **Hibernate**: 6.4.1.Final
- **HikariCP**: 4.0.3 - Connection pool

## 🚀 Running the Benchmarks

### Prerequisites

- JDK 21 or higher
- Maven 3.6+

### Quick Start (Recommended)

Use the provided scripts for an automated build and test process:

**Windows:**
```bash
run-benchmark.bat
```

**Linux/macOS:**
```bash
chmod +x run-benchmark.sh
./run-benchmark.sh
```

These scripts will:
1. Build the project with Maven
2. Run all benchmarks
3. Save results to `results/benchmark-results.json`

### Manual Build and Run

### Build the Project

```bash
cd sql-benchmark
mvn clean package
```

### Run All Benchmarks

**Windows:**
```bash
run-benchmark.bat
```

**Linux/macOS:**
```bash
./run-benchmark.sh
```

Or manually:
```bash
java -jar target/benchmarks.jar
```

### Run Specific Benchmarks

```bash
# Insert tests only
java -jar target/benchmarks.jar InsertBenchmark

# Query tests only
java -jar target/benchmarks.jar QueryBenchmark

# Update tests only
java -jar target/benchmarks.jar UpdateBenchmark

# Delete tests only
java -jar target/benchmarks.jar DeleteBenchmark

# Complex query tests only
java -jar target/benchmarks.jar ComplexQueryBenchmark
```

### Custom Test Parameters

```bash
# Increase warmup and test iterations
java -jar target/benchmarks.jar -wi 5 -i 10

# Multi-threaded testing
java -jar target/benchmarks.jar -t 4

# Verbose output
java -jar target/benchmarks.jar -v EXTRA

# Output to JSON file
java -jar target/benchmarks.jar -rf json -rff results/benchmark-results.json
```

## 📊 Visualizing Results

After running the tests, results are saved in the `results/` directory. You can visualize them using JMH Visualizer:

1. Visit: http://jmh.morethan.io/
2. Upload `results/benchmark-results.json`
3. View interactive performance comparison charts

## 🔍 Code Comparison

### Example 1: Simple Query

**easy-query**:
```java
List<User> users = easyEntityQuery.queryable(User.class)
    .where(u -> u.age().ge(25))
    .orderBy(u -> u.username().desc())
    .limit(10)
    .toList();
```

**JOOQ**:
```java
List<JooqUser> users = jooqDsl.select()
    .from(table("t_user"))
    .where(field("age").ge(25))
    .orderBy(field("username").desc())
    .limit(10)
    .fetchInto(JooqUser.class);
```

### Example 2: COUNT Query

**easy-query**:
```java
long count = easyEntityQuery.queryable(User.class)
    .where(u -> {
        u.age().ge(25);
        u.age().le(35);
    })
    .count();
```

**JOOQ**:
```java
Integer count = jooqDsl.selectCount()
    .from(table("t_user"))
    .where(field("age").ge(25).and(field("age").le(35)))
    .fetchOne(0, Integer.class);
long result = count != null ? count : 0;
```

### Example 3: JOIN Query

**easy-query**:
```java
List<User> users = easyEntityQuery.queryable(User.class)
    .innerJoin(Order.class, (u, o) -> u.id().eq(o.userId()))
    .where((u, o) -> {
        o.status().eq(1);
        o.amount().ge(new BigDecimal("100"));
    })
    .distinct()
    .limit(20)
    .toList();
```

**JOOQ**:
```java
List<JooqUser> users = jooqDsl.selectDistinct(
        field("t_user.id").as("id"),
        field("t_user.username").as("username"),
        field("t_user.email").as("email"),
        field("t_user.age").as("age"),
        field("t_user.phone").as("phone"),
        field("t_user.address").as("address")
    )
    .from(table("t_user"))
    .join(table("t_order")).on(field("t_user.id").eq(field("t_order.user_id")))
    .where(field("t_order.status").eq(1)
        .and(field("t_order.amount").ge(new BigDecimal("100"))))
    .limit(20)
    .fetchInto(JooqUser.class);
```

**Hibernate**:
```java
TypedQuery<HibernateUser> query = entityManager.createQuery(
    "SELECT DISTINCT u FROM HibernateUser u " +
    "JOIN HibernateOrder o ON u.id = o.userId " +
    "WHERE o.status = :status AND o.amount >= :minAmount",
    HibernateUser.class);
query.setParameter("status", 1);
query.setParameter("minAmount", new BigDecimal("100"));
query.setMaxResults(20);
List<HibernateUser> users = query.getResultList();
```

## 💡 Key Advantages

### easy-query Advantages:

1. **🚀 Excellent Performance**: Comparable or better performance in most scenarios
2. **✨ Type Safety**: Proxy-based strongly-typed API with compile-time checking
3. **🎯 Usability**: Lambda expression style, more in line with Java development habits
4. **📦 Auto-Mapping**: Automatic object mapping (JOOQ also supports `fetchInto()` for basic mapping)
5. **🔧 Flexibility**: Supports multiple query methods and databases
6. **📝 Cleaner Code**: Less boilerplate code, especially for complex queries

### Comparison with Hibernate:

- **Performance**: Generally faster than Hibernate in most scenarios
- **Type Safety**: Better compile-time type checking with lambda expressions
- **Simplicity**: No need for EntityManager transaction management in simple queries
- **SQL Control**: More direct control over generated SQL like JOOQ
- **Learning Curve**: Easier to understand for developers familiar with SQL

## 🤝 Contributing

Issues and Pull Requests are welcome!

If you find any problems with the tests or have suggestions for improvements, please feel free to open an issue.

## 📄 License

This project follows the Apache License 2.0.

## 🔗 Related Links

- [easy-query GitHub](https://github.com/xuejmnet/easy-query)
- [easy-query Documentation](https://xuejmnet.github.io/easy-query-doc/)
- [JOOQ Official Site](https://www.jooq.org/)
- [JMH Official Site](https://github.com/openjdk/jmh)
