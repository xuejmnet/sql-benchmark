package com.easyquery.benchmark;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;


public class DatabaseInitializer {
    
    private static volatile DataSource dataSource;
    
    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (DatabaseInitializer.class) {
                if (dataSource == null) {
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl("jdbc:h2:mem:benchmark;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE");
                    config.setUsername("sa");
                    config.setPassword("");
                    config.setMaximumPoolSize(10);
                    config.setMinimumIdle(5);
                    dataSource = new HikariDataSource(config);
                    initDatabase();
                }
            }
        }
        return dataSource;
    }
    
    private static void initDatabase() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream("schema.sql");
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().startsWith("--") && !line.trim().isEmpty()) {
                        sql.append(line).append("\n");
                    }
                }
                
                String[] sqlStatements = sql.toString().split(";");
                for (String sqlStatement : sqlStatements) {
                    if (!sqlStatement.trim().isEmpty()) {
                        stmt.execute(sqlStatement.trim());
                    }
                }
                reader.close();
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    public static void clearData() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM t_order");
            stmt.execute("DELETE FROM t_user");
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear data", e);
        }
    }
    
    public static void insertUserWithJdbc(String id, String username, String email, Integer age, String phone, String address) {
        String sql = "INSERT INTO t_user (id, username, email, age, phone, address) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, username);
            pstmt.setString(3, email);
            pstmt.setInt(4, age);
            pstmt.setString(5, phone);
            pstmt.setString(6, address);
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert user with JDBC", e);
        }
    }
    
    public static void insertOrderWithJdbc(String id, String userId, String orderNo, java.math.BigDecimal amount, Integer status, String remark) {
        String sql = "INSERT INTO t_order (id, user_id, order_no, amount, status, remark) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, userId);
            pstmt.setString(3, orderNo);
            pstmt.setBigDecimal(4, amount);
            pstmt.setInt(5, status);
            pstmt.setString(6, remark);
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert order with JDBC", e);
        }
    }
}



