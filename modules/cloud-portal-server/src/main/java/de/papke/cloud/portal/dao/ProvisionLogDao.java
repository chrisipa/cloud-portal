package de.papke.cloud.portal.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import de.papke.cloud.portal.pojo.ProvisionLog;

public interface ProvisionLogDao extends MongoRepository<ProvisionLog, String> {
	
	@Query("{ $and: [ { 'command' : ?0 }, { 'expirationDate' : { $lt: ?1 } } ] }")
	public List<ProvisionLog> findByCommandAndExpirationDate(String command, Date expirationDate);
	public ProvisionLog findById(String id);
	public List<ProvisionLog> findByGroupInAndProvider(List<String> groups, String provider);
	public List<ProvisionLog> findByProvider(String provider);
}