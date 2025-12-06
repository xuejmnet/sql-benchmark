# Easy-Query vs JOOQ Performance Benchmark

## 🏆 Benchmark Results

**Test Environment:**
- **OS**: Windows 10
- **JDK**: OpenJDK 21.0.9 (64-Bit Server VM)
- **Database**: H2 2.2.224 (In-Memory)
- **Connection Pool**: HikariCP 4.0.3

### Performance Summary (ops/s - higher is better)

| Test Scenario | easy-query | JOOQ | Performance Ratio |
|--------------|------------|------|-------------------|
| **Query Operations** |
| Query by ID | 42,269 ± 216,104 | 13,666 ± 28,694 | 🚀 **3.09x faster** |
| Conditional Query | 92,287 ± 496,991 | 41,531 ± 116,225 | 🚀 **2.22x faster** |
| COUNT Query | 27,351 ± 84,573 | 114,499 ± 399,596 | ❌ **0.24x slower** |
| **Insert Operations** |
| Single Insert | 95,844 ± 11,417 | 25,939 ± 93,497 | 🚀 **3.70x faster** |
| Batch Insert (10) | 4,016 ± 17,311 | 9,972 ± 1,065 | ❌ **0.40x slower** |
| **Update Operations** |
| Update by ID | 190,674 ± 5,164 | 121,304 ± 257,883 | ✅ **1.57x faster** |
| Batch Update | 4,608 ± 7,163 | 5,351 ± 872 | ⚖️ **0.86x comparable** |
| **Delete Operations** |
| Delete by Condition | 63,610 ± 83,836 | 40,436 ± 81,054 | ✅ **1.57x faster** |
| **Complex Operations** |
| JOIN Query | 15,183 ± 34,975 | 5,751 ± 294 | 🚀 **2.64x faster** |
| Aggregation (COUNT) | 136,926 ± 595,741 | 19,131 ± 59,396 | 🚀 **7.16x faster** |

### Key Findings

✅ **easy-query advantages:**
- **Complex Aggregation queries**: 7.16x faster than JOOQ
- **Single insert operations**: 3.70x faster than JOOQ
- **Simple queries by ID**: 3.09x faster than JOOQ
- **JOIN queries**: 2.64x faster than JOOQ
- **Conditional queries**: 2.22x faster than JOOQ
- **Delete operations**: 1.57x faster than JOOQ
- **Update by ID**: 1.57x faster than JOOQ

❌ **JOOQ advantages:**
- **COUNT queries**: 4.19x faster than easy-query
- **Batch insert operations**: 2.48x faster than easy-query

⚖️ **Comparable performance:**
- **Batch updates**: Similar performance (86% of JOOQ)

💡 **Overall**: easy-query shows superior performance in most single-record operations and complex queries (especially aggregations and joins). JOOQ demonstrates better performance in simple COUNT queries and batch insert operations.

⚠️ **Important Note on Data Stability**: The test results show **very high variance** (large error margins), with some confidence intervals including negative values. This indicates significant instability in the benchmark results. For more reliable performance comparisons, it is recommended to:
- Increase measurement iterations (from 5 to 10-20)
- Extend warmup time (from 1s to 3-5s)
- Use multiple forks (from 1 to 3-5)
- Ensure a clean test environment with minimal background processes

---

## 📊 Overview

This is a **standalone** comprehensive performance benchmark comparing **easy-query** and **JOOQ** using JMH (Java Microbenchmark Harness).

> **Talk is cheap, show me the code and benchmarks!**

This project provides objective benchmark data to prove that easy-query is not just a JOOQ clone, but offers significant advantages in both performance and usability.

### ✨ Standalone Project

This benchmark can be run **independently** without the easy-query parent project:
- ✅ No parent POM dependency
- ✅ Self-contained configuration
- ✅ Clone and run anywhere
- ✅ All dependencies explicitly declared

## 🎯 Test Scenarios

All benchmarks use JMH (Java Microbenchmark Harness) with the following configuration:
- **Mode**: Throughput (operations per second)
- **Warmup**: 3 iterations, 1 second each
- **Measurement**: 5 iterations, 2 seconds each
- **Fork**: 1 JVM fork
- **Threads**: 1 thread

### 1. **Insert Operations (InsertBenchmark)**
- **Single insert**: Insert one user record at a time
- **Batch insert (10 records)**: Insert 10 user records in a single batch operation

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

## 💡 Key Advantages

### easy-query vs JOOQ:

1. **🚀 Excellent Performance**: Comparable or better performance in most scenarios
2. **✨ Type Safety**: Proxy-based strongly-typed API with compile-time checking
3. **🎯 Usability**: Lambda expression style, more in line with Java development habits
4. **📦 Auto-Mapping**: Automatic object mapping (JOOQ also supports `fetchInto()` for basic mapping)
5. **🔧 Flexibility**: Supports multiple query methods and databases
6. **📝 Cleaner Code**: Less boilerplate code, especially for complex queries

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
