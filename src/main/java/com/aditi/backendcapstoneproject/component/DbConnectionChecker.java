package com.aditi.backendcapstoneproject.component;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

@Component
public class DbConnectionChecker { //this is just to check either database is connected or not and to your db

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void printDbUsed() throws SQLException {
        System.out.println("Connected to DB: " + dataSource.getConnection().getCatalog());
    }

}

