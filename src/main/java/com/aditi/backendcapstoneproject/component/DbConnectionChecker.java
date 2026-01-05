package com.aditi.backendcapstoneproject.component;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

@Component
public class DbConnectionChecker {

    private static final Logger logger = LoggerFactory.getLogger(DbConnectionChecker.class);

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void printDbUsed() throws SQLException {
        logger.info("Connected to DB: {}", dataSource.getConnection().getCatalog());
    }

}

