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
import com.oou.has.model.Hostel;
import com.oou.has.model.PagedList;
import com.oou.has.model.Type;

/**
 * @author AAfolayan
 *
 */
@Stateless
public class HostelService {
	
	@Inject 
	RoomService roomService;

	@PersistenceContext(unitName = "app")
	private EntityManager em;

	private static Logger LOG = LoggerFactory.getLogger(HostelService.class);

	public void createHostel(Hostel e) {
		em.persist(e);
		em.flush();
	}

	public Hostel updateHostel(Hostel e) {
		return em.merge(e);
	}

	public Hostel findHostel(Long id) {
		return em.find(Hostel.class, id);
	}

	public PagedList<Hostel> fetchHostel(int first, int pageSize) {
		PagedList<Hostel> list = new PagedList<Hostel>();
		TypedQuery<Hostel> query = em.createQuery("select s from Hostel s order by s.createdDate desc", Hostel.class);
		query.setFirstResult(first).setMaxResults(pageSize);
		List<Hostel> res = query.getResultList();
		list.setList(res);

		Number count = fetchHostelCount();
		list.setCount(count.intValue());

		return list;
	}

	public Number fetchHostelCount() {
		TypedQuery<Number> query = em.createQuery("select count(s.id) from Hostel s", Number.class);
		Number res = query.getSingleResult();
		return res;
	}

	public List<Hostel> fetchHostel() {
		TypedQuery<Hostel> query = em.createQuery("select s from Hostel s order by s.createdDate desc", Hostel.class);
		List<Hostel> res = query.getResultList();
		return res;
	}

	public List<Hostel> getAvailableHostel(String gender) {
		System.out.println("gender is --->>> "+gender );
		TypedQuery<Hostel> query = em.createQuery("select s from Hostel s WHERE s.type =:gender", Hostel.class);
		if (gender.equals("MALE")) {
		query.setParameter("gender", Type.BOYS);
		}
		if (gender.equals("FEMALE")) {
			query.setParameter("gender", Type.GIRLS);
			}
		List<Hostel> hostels = query.getResultList();
		return hostels;
		
	}

}
