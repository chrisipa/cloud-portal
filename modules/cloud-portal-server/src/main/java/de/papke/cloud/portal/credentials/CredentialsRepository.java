package de.papke.cloud.portal.credentials;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CredentialsRepository extends MongoRepository<Credentials, String> {
    public Credentials findByGroup(String group);
}
