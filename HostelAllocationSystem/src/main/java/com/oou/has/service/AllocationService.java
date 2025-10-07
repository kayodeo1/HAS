/**
 * 
 */
package com.oou.has.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.io.FileUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.oou.has.model.Allocation;
import com.oou.has.model.PagedList;
import com.oou.has.model.User;

/**
 * @author AAfolayan
 *
 */
@Stateless
public class AllocationService {

	@PersistenceContext(unitName = "app")
	private EntityManager em;


	private static Logger LOG = LoggerFactory.getLogger(AllocationService.class);

	public void createAllocation(Allocation e){
		em.persist(e);
		em.flush();
	}

	public Allocation updateAllocation(Allocation e){
		return em.merge(e);
	}
	
	public Allocation findAllocation(Long id){
		return em.find(Allocation.class, id);
	}
	
	public PagedList<Allocation> fetchAllocation(int first, int pageSize){
		PagedList<Allocation> list = new PagedList<Allocation>();
		TypedQuery<Allocation> query = em.createQuery(
				"select s from Allocation s order by s.createdDate desc",
				Allocation.class);
		query.setFirstResult(first).setMaxResults(pageSize);
		List<Allocation> res = query.getResultList();
		list.setList(res);

		Number count = fetchAllocationCount();
		list.setCount(count.intValue());

		return list;
	}
	public PagedList<Allocation> fetchAllocation(int first, int pageSize,String sortField, String sortDirection,
			Map<String, Object> filters){
		PagedList<Allocation> list = new PagedList<Allocation>();
		StringBuilder jpql = new StringBuilder("select s from Allocation s WHERE true=true");

		for (String key : filters.keySet()) {
			System.out.println(key);
			jpql.append(" AND LOWER(s." + key + ") LIKE LOWER(CONCAT('%', :" + key + ", '%'))");
		}

		if (sortField != null && !sortField.isEmpty()) {
			jpql.append(" ORDER BY s.").append(sortField).append(" ").append(sortDirection);
		}
		TypedQuery<Allocation> query = em.createQuery(jpql.toString(), Allocation.class);
		query.setFirstResult(first).setMaxResults(pageSize);
		for (Map.Entry<String, Object> entry : filters.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue().toString());
		}
		
		List<Allocation> res = query.getResultList();
		list.setList(res);

		Number count = fetchAllocationCount(sortDirection, filters);
		list.setCount(count.intValue());

		return list;
	}
	
		private Number fetchAllocationCount(String sortDirection, Map<String, Object> filters) {
			StringBuilder jpql = new StringBuilder("select count(s.id) from Allocation s WHERE true=true");

			for (String key : filters.keySet()) {
				jpql.append(" AND LOWER(s." + key + ") LIKE LOWER(CONCAT('%', :" + key + ", '%'))");
			}
			TypedQuery<Number> query = em.createQuery(jpql.toString(), Number.class);
			
			for (Map.Entry<String, Object> entry : filters.entrySet()) {
				query.setParameter(entry.getKey(), entry.getValue().toString());
			}
			Number res = query.getSingleResult();
			return res;
	}

		public Number fetchAllocationCount()
			{
		TypedQuery<Number> query = em.createQuery(
				"select count(s.id) from Allocation s",
				Number.class);
		Number res = query.getSingleResult();
		return res;
	}

		public List<String> fetchRoomOccupants(Long id) {
			TypedQuery<String> query = em.createQuery(
					"select s.userName from Allocation s WHERE s.roomId =:roomId",String.class).setParameter("roomId", id);
			List<String> users = query.getResultList();
			return users;
		}

}
