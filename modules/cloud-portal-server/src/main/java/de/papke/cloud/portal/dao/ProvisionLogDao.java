package de.papke.cloud.portal.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import de.papke.cloud.portal.pojo.ProvisionLog;

public interface ProvisionLogDao extends MongoRepository<ProvisionLog, String> {
	public ProvisionLog findById(String id);
	public List<ProvisionLog> findByUsernameAndProvider(String username, String provider);
}