package de.papke.cloud.portal.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import de.papke.cloud.portal.pojo.ProvisionLog;

public interface ProvisionLogDao extends MongoRepository<ProvisionLog, String> {
	
	@Query("{ $and: [ { 'success' : ?0 }, { 'command' : ?1 }, { 'expirationDate' : { $lt: ?2 } } ] }")
	public List<ProvisionLog> findBySuccessAndCommandAndExpirationDate(boolean success, String command, Date expirationDate);
	public ProvisionLog findById(String id);
	public List<ProvisionLog> findByUseCaseId(String useCaseId);
	public List<ProvisionLog> findByGroupInAndUseCaseId(List<String> groups, String useCaseId);
	public List<ProvisionLog> findByProvider(String provider);
	public List<ProvisionLog> findByGroupInAndProvider(List<String> groups, String provider);
}