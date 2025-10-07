package com.oou.has.service;

/**
 * @author Afolayana
 *
 */

import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import com.oou.has.model.PagedList;
import com.oou.has.model.Status;
import com.oou.has.model.User;


@Stateless
public class UserService {

	@PersistenceContext(unitName = "app")
	private EntityManager em;
	// @Inject
	// NewCredentialsEventProcessor mailSender;
	// @Inject
	// private NotificationEventProcessor eventProcessor;
//	@EJB
//	private MailService mailService;

//	@AuditLog
//	public Long unsubscribe(User user) {
//		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
//		String suffix = df.format(new Date());
//		user.setUsername(suffix + "#" + user.getUsername());
//		user.setEmail(suffix + "#" + user.getEmail());
//		user.setMeterNumber(suffix + "#" + user.getMeterNumber());
//		user.setStatus(Status.DISABLED);
//		em.merge(user);
//		em.flush();
//		return user.getId();
	
//	}
	public Number fetchUserCount() throws Exception {
		TypedQuery<Number> query = em.createQuery("select count(s.id) from User s ", Number.class);
		Number res = query.getSingleResult();
		return res;
	}

	public PagedList<User> fetchUser( int first, int pageSize) throws Exception {
		PagedList<User> list = new PagedList<>();
		TypedQuery<User> query = em.createQuery("select s from User s order by s.createdDate desc", User.class);
		query.setFirstResult(first).setMaxResults(pageSize);
		List<User> res = query.getResultList();
		list.setList(res);

		Number count = fetchUserCount();
		list.setCount(count.intValue());

		return list;
	}
	public Number fetchUserCount( String sortDirection, Map<String, Object> filters) throws Exception {
		StringBuilder jpql = new StringBuilder("select count(s.id) from User s WHERE true=true");

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

	public PagedList<User> fetchUser( int first, int pageSize, String sortField, String sortDirection,
			Map<String, Object> filters) throws Exception {
		PagedList<User> list = new PagedList<>();
		StringBuilder jpql = new StringBuilder("select s from User s WHERE true=true");

		for (String key : filters.keySet()) {
			System.out.println(key);
			jpql.append(" AND LOWER(s." + key + ") LIKE LOWER(CONCAT('%', :" + key + ", '%'))");
		}

		if (sortField != null && !sortField.isEmpty()) {
			jpql.append(" ORDER BY s.").append(sortField).append(" ").append(sortDirection);
		}
		System.out.println(jpql.toString());
		TypedQuery<User> query = em.createQuery(jpql.toString(), User.class);
		query.setFirstResult(first).setMaxResults(pageSize);
		for (Map.Entry<String, Object> entry : filters.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue().toString());
		}
		List<User> res = query.getResultList();
		list.setList(res);

		Number count = fetchUserCount( sortDirection, filters);
		list.setCount(count.intValue());

		return list;
	}







//	@AuditLog
	public Long create(User user) {
		// System.out.println("EJB CREATE INVOKED!");
		em.persist(user);
		em.flush();
		// create mail notification event entry
		// NotificationEvent evt = createEvent(user);
		// eventProcessor.create(evt);
		return user.getId();
	}

	public User update(User user) {
		return em.merge(user);
	}

	public User findByUsername(String username) {
		List<User> found = em.createQuery("select u from User u where u.matricNumber=:username", User.class)
				.setParameter("username", username).getResultList();
		return found.isEmpty() ? null : found.get(0);
	}

	public void updateRole(User user) {
		System.out.println("fetching user with id........");
		System.out.println(user.getId());
		
		User u = find(user.getId());
		u.setRoles(user.getRoles());
		em.merge(u);
		em.flush();
	}

	private User find(Long id) {
		return em.find(User.class, id);
		

	}


}