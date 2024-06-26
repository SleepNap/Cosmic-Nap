package org.gms.tools.mapletools;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.gms.tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

final class SimpleDatabaseConnection {
    private SimpleDatabaseConnection() {}

    static Connection getConnection() {
        muffleLogging();
        DatabaseConnection.initializeConnectionPool();

        try {
            return DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get database connection", e);
        }
    }

    private static void muffleLogging() {
        final Level minimumVisibleLevel = Level.WARN;
        Configurator.setLevel(LogManager.getLogger(DruidDataSource.class).getName(), minimumVisibleLevel);
        Configurator.setRootLevel(minimumVisibleLevel);
    }
}
