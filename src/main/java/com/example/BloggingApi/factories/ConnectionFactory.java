package com.example.BloggingApi.factories;

import com.example.BloggingApi.DbInterfaces.IConnection;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class ConnectionFactory implements IConnection {


    public Connection createConnection() {
        try {
            String url = "jdbc:postgresql://localhost:5432/springDb";
            String username = "philus";
            String password = "philus";
            Connection connection =
                    DriverManager.getConnection(url, username, password);

            System.out.println("Connected to the database successfully.");
            return connection;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create database connection", e);
        }
    }
}
