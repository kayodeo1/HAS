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
import com.oou.has.model.Transaction;
import com.oou.has.model.User;
import com.oou.has.model.PagedList;
import com.oou.has.model.Status;

/**
 * @author AAfolayan
 *
 */
@Stateless
public class TransactionService {

	@PersistenceContext(unitName = "app")
	private EntityManager em;


	private static Logger LOG = LoggerFactory.getLogger(TransactionService.class);

	public void createTransaction(Transaction e){
		em.persist(e);
		em.flush();
	}

	public Transaction updateTransaction(Transaction e){
		return em.merge(e);
	}
	
	public Transaction findTransaction(Long id){
		return em.find(Transaction.class, id);
	}
	public PagedList<Transaction> fetchTransaction(int first, int pageSize,Long id){
		PagedList<Transaction> list = new PagedList<Transaction>();
		TypedQuery<Transaction> query = em.createQuery(
				"select s from Transaction s WHERE s.userId =:id order by s.createdDate desc",
				Transaction.class).setParameter("id", id);
		query.setFirstResult(first).setMaxResults(pageSize);
		List<Transaction> res = query.getResultList();
		list.setList(res);

		Number count = fetchTransactionCount(id);
		list.setCount(count.intValue());

		return list;
	}
	
	public PagedList<Transaction> fetchTransaction(int first, int pageSize){
		PagedList<Transaction> list = new PagedList<Transaction>();
		TypedQuery<Transaction> query = em.createQuery(
				"select s from Transaction s order by s.createdDate desc",
				Transaction.class);
		query.setFirstResult(first).setMaxResults(pageSize);
		List<Transaction> res = query.getResultList();
		list.setList(res);

		Number count = fetchTransactionCount();
		list.setCount(count.intValue());

		return list;
	}
	
	public PagedList<Transaction> fetchTransaction(int first, int pageSize, String sortField, String sortDirection,
			Map<String, Object> filters){
		StringBuilder jpql = new StringBuilder("select s from  Transaction s WHERE true=true");

		for (String key : filters.keySet()) {
			jpql.append(" AND LOWER(s." + key + ") LIKE LOWER(CONCAT('%', :" + key + ", '%'))");
		}

		if (sortField != null && !sortField.isEmpty()) {
			jpql.append(" ORDER BY s.").append(sortField).append(" ").append(sortDirection);
		}
		TypedQuery<Transaction> query = em.createQuery(jpql.toString(), Transaction.class);
		query.setFirstResult(first).setMaxResults(pageSize);
		for (Map.Entry<String, Object> entry : filters.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue().toString());
		}
		
		PagedList<Transaction> list = new PagedList<Transaction>();
		List<Transaction> res = query.getResultList();
		list.setList(res);

		Number count = fetchTransactionCount(sortDirection, filters);
		list.setCount(count.intValue());

		return list;
	}
	
	
	
	
	
	private Number fetchTransactionCount(String sortDirection, Map<String, Object> filters) {
		StringBuilder jpql = new StringBuilder("select count(s.id) from Transaction s WHERE true=true");

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

	public Number fetchTransactionCount(Long id)
	{
TypedQuery<Number> query = em.createQuery(
		"select count(s.id) from Transaction s WHERE s.userId =:id",
		Number.class).setParameter("id", id);
Number res = query.getSingleResult();
return res;
}
		public Number fetchTransactionCount()
			{
		TypedQuery<Number> query = em.createQuery(
				"select count(s.id) from Transaction s",
				Number.class);
		Number res = query.getSingleResult();
		return res;
	}

		public Transaction findTransactionByPaymentReference(String paymentReferenceFromRequest) {
			TypedQuery<Transaction> query = em.createQuery(
					"select s from Transaction s WHERE s.paymentReference =:paymentReference",
					Transaction.class).setParameter("paymentReference", paymentReferenceFromRequest);
			return query.getSingleResult();			
		}

		public Double fetchTotalAmount() {
			TypedQuery<Double> query = em.createQuery(
					"select sum(s.amount) from Transaction s WHERE s.status =:status",
					Double.class).setParameter("status", Status.SUCCESSFUL);
			return query.getSingleResult();
		}
		
		public Number fetchSuccessfulTransactionCount() {
			TypedQuery<Number> query = em.createQuery(
					"select count(s.id) from Transaction s WHERE s.status=:status",
					Number.class).setParameter("status", Status.SUCCESSFUL);
			Number res = query.getSingleResult();
			return res;
		}

		public Number fetchFailedransactionCount() {
			TypedQuery<Number> query = em.createQuery(
					"select count(s.id) from Transaction s WHERE s.status =:status",
					Number.class).setParameter("status", Status.FAILED);
			Number res = query.getSingleResult();
			return res;
		}
}
