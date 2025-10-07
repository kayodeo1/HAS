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
import com.oou.has.model.Room;
import com.oou.has.model.Transaction;
import com.oou.has.model.Allocation;
import com.oou.has.model.Hostel;
import com.oou.has.model.PagedList;

/**
 * @author AAfolayan
 *
 */
@Stateless
public class RoomService {

	@PersistenceContext(unitName = "app")
	private EntityManager em;
	@Inject
	AllocationService allocationService;

	private static Logger LOG = LoggerFactory.getLogger(RoomService.class);

	public void createRoom(Room e) {
		em.persist(e);
		em.flush();
	}

	public void createRoom(Room e, int n, int lastRoomNumber) {
		for (int i = 1; i <= n; i++) {
			Room r = new Room();
			r.setHostelId(e.getHostelId());
			r.setAmount(e.getAmount());
			r.setSize(e.getSize());
			r.setHostelName(e.getHostelName());
			r.setType(e.getType());
			r.setDescription(e.getDescription());
			r.setRoomNumber(lastRoomNumber + 1);
			em.persist(r);
			lastRoomNumber = lastRoomNumber + 1;

		}
		em.flush();
	}

	public Room updateRoom(Room e) {
		return em.merge(e);
	}

	public Room findRoom(Long id) {
		return em.find(Room.class, id);
	}

	public Room fetchLastRoom(long hostelId) {
		System.out.println("fetching last room for hostel id: " + hostelId + "");
		TypedQuery<Room> query = em.createQuery(
				"select s from Room s where s.hostelId = :hostelId order by s.roomNumber desc", Room.class);
		query.setParameter("hostelId", hostelId);
		query.setMaxResults(1);
		List<Room> res = query.getResultList();
		if (res.size() > 0)
			return res.get(0);

		return null;

	}

	public PagedList<Room> fetchRoom(int first, int pageSize) {
		PagedList<Room> list = new PagedList<Room>();
		TypedQuery<Room> query = em.createQuery("select s from Room s order by s.createdDate desc", Room.class);
		query.setFirstResult(first).setMaxResults(pageSize);
		List<Room> res = query.getResultList();
		list.setList(res);

		Number count = fetchRoomCount();
		list.setCount(count.intValue());

		return list;
	}
	
	public PagedList<Room> fetchRoom(Long  hostelId,int first, int pageSize, String sortField, String sortDirection,
			Map<String, Object> filters) {
		StringBuilder jpql = new StringBuilder("select s from  Room s WHERE s.hostelId=:hostelId");

		for (String key : filters.keySet()) {
			jpql.append(" AND LOWER(s." + key + ") LIKE LOWER(CONCAT('%', :" + key + ", '%'))");
		}

		if (sortField != null && !sortField.isEmpty()) {
			jpql.append(" ORDER BY s.").append(sortField).append(" ").append(sortDirection);
		}
		TypedQuery<Room> query = em.createQuery(jpql.toString(), Room.class);
		query.setFirstResult(first).setMaxResults(pageSize);
		for (Map.Entry<String, Object> entry : filters.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue().toString());
		}
		query.setParameter("hostelId", hostelId);
		
		PagedList<Room> list = new PagedList<Room>();
		List<Room> res = query.getResultList();
		list.setList(res);

		Number count = fetchRoomCount( hostelId ,sortField,filters);
		list.setCount(count.intValue());

		return list;
	}

	private Number fetchRoomCount(Long hostelId, String sortField, Map<String, Object> filters) {
		StringBuilder jpql = new StringBuilder("select count(s.id) from Room s WHERE s.hostelId=:id");

		for (String key : filters.keySet()) {
			jpql.append(" AND LOWER(s." + key + ") LIKE LOWER(CONCAT('%', :" + key + ", '%'))");
		}
		TypedQuery<Number> query = em.createQuery(jpql.toString(), Number.class);
		for (Map.Entry<String, Object> entry : filters.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue().toString());
		}
		query.setParameter("id", hostelId);
		Number res = query.getSingleResult();
		return res;
	}

	public PagedList<Room> fetchRoom(int first, int pageSize, long hostelId) {
		PagedList<Room> list = new PagedList<Room>();
		TypedQuery<Room> query = em.createQuery("select s from Room s WHERE s.hostelId =:id order by s.roomNumber desc",
				Room.class);
		query.setFirstResult(first).setMaxResults(pageSize);
		query.setParameter("id", hostelId);
		List<Room> res = query.getResultList();
		list.setList(res);

		Number count = fetchRoomCount(hostelId);
		list.setCount(count.intValue());

		return list;
	}

	public Number fetchRoomCount() {
		TypedQuery<Number> query = em.createQuery("select count(s.id) from Room s", Number.class);
		Number res = query.getSingleResult();
		return res;
	}
	public Number fetchAvailableRoomCount() {
		TypedQuery<Number> query = em.createQuery("select count(s.id) from Room s WHERE  s.isFull  =:isFull", Number.class).setParameter("isFull", false);
		Number res = query.getSingleResult();
		return res;
	}
	public Number fetchUnavailableRoomCount() {
		TypedQuery<Number> query = em.createQuery("select count(s.id) from Room s WHERE  s.isFull  =:isFull", Number.class).setParameter("isFull", true);
		Number res = query.getSingleResult();
		return res;
	}
	public Number fetchRoomCount(Long id) {
		TypedQuery<Number> query = em.createQuery("select count(s.id) from Room s WHERE s.hostelId =:id", Number.class).setParameter("id", id);
		Number res = query.getSingleResult();
		return res;
	}

	public List<Room> getAvailableRooms(Long id) {
		TypedQuery<Room> query = em.createQuery("select s from Room s WHERE s.hostelId =:id", Room.class);
		query.setParameter("id", id);
		List<Room> rooms = query.getResultList();
		return rooms;
	}

	public List<Room> findMostSuitableRoom(Transaction t) {
		TypedQuery<Room> query = em
				.createQuery("select s from Room s WHERE s.hostelId =:hostelId AND s.isFull  =:isFull", Room.class)
				.setParameter("hostelId", t.getHostelId()).setParameter("isFull", false);
		List<Room> availableRooms = query.getResultList();
		System.out.println("number of available rooms --->>>"+availableRooms.size());
		return availableRooms;
	}

	public Room updateRoom(Room selectedRoom, Allocation allocation) {
		List<String> allocations = allocationService.fetchRoomOccupants(selectedRoom.getId());
		if (allocations !=null && allocations.size() == selectedRoom.getSize()) {
			selectedRoom.setIsFull(true);
		}
		return em.merge(selectedRoom);
		
	}

}
