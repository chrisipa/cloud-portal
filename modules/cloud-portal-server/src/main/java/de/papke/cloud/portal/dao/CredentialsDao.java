package de.papke.cloud.portal.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.papke.cloud.portal.pojo.Credentials;

public interface CredentialsDao extends MongoRepository<Credentials, String> {
	public List<Credentials> findByProvider(String provider);
    public Credentials findByGroupAndProvider(String group, String provider);
}
