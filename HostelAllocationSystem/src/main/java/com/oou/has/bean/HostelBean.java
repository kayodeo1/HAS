/**
 * 
 */
package com.oou.has.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.Flash;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oou.has.model.QueryType;
import com.oou.has.model.Constants;
import com.oou.has.lazymodel.HostelLazyDataModel;
import com.oou.has.model.Hostel;
import com.oou.has.model.Role;
import com.oou.has.model.User;
import com.oou.has.service.HostelService;
import com.oou.has.service.UserService;

/**
 * @author AAfolayan
 *
 */
@Named("hostelBean")
@ViewScoped
public class HostelBean implements Serializable {

    public static final String APP_BASE_NAME = Constants.APP_BASE_NAME;
	private static Logger LOG = LoggerFactory.getLogger(HostelBean.class);
	@Inject
	private UserService userService;
	@Inject
	private HostelService hostelService;
	private Hostel entry = new Hostel();
	private LazyDataModel<Hostel> lazyModel;
	private List<Hostel> featuredHostels;
	
	private static final String HOSTEL_MGT_URL = APP_BASE_NAME + "/online/hostel/hostels.xhtml?faces-redirect=true";
	private static final String HOSTEL_CREATION_URL = APP_BASE_NAME + "/online/hostel/create.xhtml?faces-redirect=true";
	private static final String ROOM_CREATION_URL = APP_BASE_NAME + "/online/hostel/roomList.xhtml?faces-redirect=true";


	private List<Hostel> entries = new ArrayList<>();
																		
	
	private User user = new User();

	@PostConstruct
	public void init() {
		LOG.info("HostelBean init!");
		featuredHostels = hostelService.fetchHostel();
		System.out.println("hostels fetched ---->>>>"+featuredHostels.size());
	}
	
	public void listHostel() {
		LOG.info("listHostel invoked!!!");
		try {
			//SecurityUtils.getSubject().checkRole("MDOPS");
			lazyModel = new HostelLazyDataModel(hostelService, QueryType.GET_ALL_HOSTEL);
		} catch (Exception e) {
			Messages.addGlobalError("oops error encountered while fetching entries!");
			LOG.error("oops error encountered while fetching entries!", e.fillInStackTrace());
			e.printStackTrace(); //
		}
	}


	public void updateHostel() {
		LOG.info("updateHostel invoked!!!");
		try {
			//SecurityUtils.getSubject().checkRole("MDOPS");
			LOG.info("entry -> " + entry);
			hostelService.updateHostel(entry);
			Messages.addFlashGlobalInfo("Hostel update request processed successfully!");
			entry = new Hostel();
			Faces.redirect(HOSTEL_MGT_URL);
		} catch (Exception e) {
			Messages.addGlobalError("Hostel update request failed!");
			LOG.error("Hostel update request failed!", e.fillInStackTrace());
			e.printStackTrace(); // TODO: logger.
			// return null;
		}
	}
	
	
		public void createHostel() {
		LOG.info("createHostel invoked!!!");
		try {
			//SecurityUtils.getSubject().checkRole("MDOPS");
			LOG.info("entry -> " + entry);
			hostelService.createHostel(entry);
			Messages.addFlashGlobalInfo("Hostel creation request processed successfully!");
			entry = new Hostel();
			Faces.redirect(HOSTEL_MGT_URL);
		} catch (Exception e) {
			Messages.addGlobalError("Hostel creation failed!");
			LOG.error("Hostel creation failed!", e.fillInStackTrace());
			e.printStackTrace(); // TODO: logger.
			// return null;
		}
	}
	
	public void createNewHostelView() throws IOException {
		LOG.info("createNewHostelView invoked");
		this.entry=new Hostel();
	}
	
  public void createHostelView() throws IOException {
		LOG.info("createHostelView invoked");
		Faces.redirect(HOSTEL_CREATION_URL);
	}
	
		public void displayHostelDialog(Hostel e) {
		LOG.info("displayHostelDialog invoked!");
		this.entry = e;
		LOG.info("entry selected:  id -> " + this.entry.getId());

	}
	
		public void prepare() {
		LOG.info("prepare method invoked!");
		Flash flash = Faces.getFlash();// FacesContext.getCurrentInstance().getExternalContext().getFlash();
		this.entry = (Hostel) flash.get("entry");
		LOG.info("selected Hostel retrieved >>> " + entry);
	}
	
		private User getCurrentUser() {
		Subject subject = SecurityUtils.getSubject();
		String username = String.valueOf(subject.getPrincipal());
		return userService.findByUsername(username);
	}
	
		public void viewHostel(Long id) throws IOException {
			LOG.info("viewing hostel----->>"+id);
			Flash flash = Faces.getFlash();
			Hostel h = hostelService.findHostel(id);
			flash.put("hostel",h );
			Faces.redirect(ROOM_CREATION_URL);
		}
	
		/**
	 * @return the entry
	 */
	public Hostel getEntry() {
		return entry;
	}

	/**
	 * @param entry the entry to set
	 */
	public void setEntry(Hostel entry) {
		this.entry = entry;
	}
	
		/**
	 * @return the entries
	 */
	public List<Hostel> getEntries() {
		return entries;
	}

	/**
	 * @param entries the entries to set
	 */
	public void setEntries(List<Hostel> entries) {
		this.entries = entries;
	}
	
		/**
	 * @return the lazyModel
	 */
	public LazyDataModel<Hostel> getLazyModel() {
		return lazyModel;
	}

	/**
	 * @param lazyModel the lazyModel to set
	 */
	public void setLazyModel(LazyDataModel<Hostel> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public List<Hostel> getFeaturedHostels() {
		return featuredHostels;
	}

	public void setFeaturedHostels(List<Hostel> featuredHostels) {
		this.featuredHostels = featuredHostels;
	}
	
	

}
