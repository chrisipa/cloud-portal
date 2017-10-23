package de.papke.cloud.portal.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.papke.cloud.portal.pojo.Credentials;

public interface CredentialsDao extends MongoRepository<Credentials, String> {
    public Credentials findByGroupAndProvider(String group, String provider);
}
