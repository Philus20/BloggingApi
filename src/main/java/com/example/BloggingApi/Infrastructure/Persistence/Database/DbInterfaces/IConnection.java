package com.example.BloggingApi.Infrastructure.Persistence.Database.DbInterfaces;

import java.sql.Connection;

public interface IConnection {
    public Connection createConnection() ;
}
