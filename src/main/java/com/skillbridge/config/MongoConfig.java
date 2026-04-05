// src/main/java/com/skillbridge/config/MongoConfig.java
package com.skillbridge.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.skillbridge.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/skillbridge}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database:skillbridge}")
    private String databaseName;

    /**
     * Tell Spring which database to use.
     */
    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    /**
     * Create the MongoClient using the URI from application.yml.
     */
    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    /**
     * MongoTemplate – used in MatchingService for aggregation queries.
     * Removes the "_class" field that Spring Data adds by default to documents.
     */
    @Override
    public MongoTemplate mongoTemplate(MongoDatabaseFactory factory,
                                       MappingMongoConverter converter) {
        // Remove _class field from all saved documents
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(factory, converter);
    }

    /**
     * Enable MongoDB transactions (requires replica set).
     * Safe to keep even without a replica set – just don't use @Transactional
     * unless you set one up.
     */
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);
    }
}