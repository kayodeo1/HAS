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
import com.oou.has.lazymodel.AllocationLazyDataModel;
import com.oou.has.model.Allocation;
import com.oou.has.model.Role;
import com.oou.has.model.User;
import com.oou.has.service.AllocationService;
import com.oou.has.service.UserService;

/**
 * @author AAfolayan
 *
 */
@Named("allocationBean")
@ViewScoped
public class AllocationBean implements Serializable {

    public static final String APP_BASE_NAME = Constants.APP_BASE_NAME;
	private static Logger LOG = LoggerFactory.getLogger(AllocationBean.class);
	@Inject
	private UserService userService;
	@Inject
	private AllocationService allocationService;
	private Allocation entry = new Allocation();
	private LazyDataModel<Allocation> lazyModel;
	
	private static final String ALLOCATION_MGT_URL = APP_BASE_NAME + "/online/allocation/list.xhtml?faces-redirect=true";
	private static final String ALLOCATION_CREATION_URL = APP_BASE_NAME + "/online/allocation/create.xhtml?faces-redirect=true";

	private List<Allocation> entries = new ArrayList<>();
																		
	
	private User user = new User();

	@PostConstruct
	public void init() {
		LOG.info("AllocationBean init!");
	}
	
	public void listAllocation() {
		LOG.info("listAllocation invoked!!!");
		try {
			//SecurityUtils.getSubject().checkRole("MDOPS");
			lazyModel = new AllocationLazyDataModel(allocationService, QueryType.GET_ALL_ALLOCATION);
		} catch (Exception e) {
			Messages.addGlobalError("oops error encountered while fetching entries!");
			LOG.error("oops error encountered while fetching entries!", e.fillInStackTrace());
			e.printStackTrace(); //
		}
	}


	public void updateAllocation() {
		LOG.info("updateAllocation invoked!!!");
		try {
			//SecurityUtils.getSubject().checkRole("MDOPS");
			LOG.info("entry -> " + entry);
			allocationService.updateAllocation(entry);
			Messages.addFlashGlobalInfo("Allocation update request processed successfully!");
			entry = new Allocation();
			Faces.redirect(ALLOCATION_MGT_URL);
		} catch (Exception e) {
			Messages.addGlobalError("Allocation update request failed!");
			LOG.error("Allocation update request failed!", e.fillInStackTrace());
			e.printStackTrace(); // TODO: logger.
			// return null;
		}
	}
	
	
		public void createAllocation() {
		LOG.info("createAllocation invoked!!!");
		try {
			//SecurityUtils.getSubject().checkRole("MDOPS");
			LOG.info("entry -> " + entry);
			allocationService.createAllocation(entry);
			Messages.addFlashGlobalInfo("Allocation creation request processed successfully!");
			entry = new Allocation();
			Faces.redirect(ALLOCATION_MGT_URL);
		} catch (Exception e) {
			Messages.addGlobalError("Allocation creation failed!");
			LOG.error("Allocation creation failed!", e.fillInStackTrace());
			e.printStackTrace(); // TODO: logger.
			// return null;
		}
	}
	
	public void createNewAllocationView() throws IOException {
		LOG.info("createNewAllocationView invoked");
		this.entry=new Allocation();
	}
	
  public void createAllocationView() throws IOException {
		LOG.info("createAllocationView invoked");
		Faces.redirect(ALLOCATION_CREATION_URL);
	}
	
		public void displayAllocationDialog(Allocation e) {
		LOG.info("displayAllocationDialog invoked!");
		this.entry = e;
		LOG.info("entry selected:  id -> " + this.entry.getId());

	}
	
		public void prepare() {
		LOG.info("prepare method invoked!");
		Flash flash = Faces.getFlash();// FacesContext.getCurrentInstance().getExternalContext().getFlash();
		this.entry = (Allocation) flash.get("entry");
		LOG.info("selected Allocation retrieved >>> " + entry);
	}
	
		private User getCurrentUser() {
		Subject subject = SecurityUtils.getSubject();
		String username = String.valueOf(subject.getPrincipal());
		return userService.findByUsername(username);
	}
	
	
		/**
	 * @return the entry
	 */
	public Allocation getEntry() {
		return entry;
	}

	/**
	 * @param entry the entry to set
	 */
	public void setEntry(Allocation entry) {
		this.entry = entry;
	}
	
		/**
	 * @return the entries
	 */
	public List<Allocation> getEntries() {
		return entries;
	}

	/**
	 * @param entries the entries to set
	 */
	public void setEntries(List<Allocation> entries) {
		this.entries = entries;
	}
	
		/**
	 * @return the lazyModel
	 */
	public LazyDataModel<Allocation> getLazyModel() {
		return lazyModel;
	}

	/**
	 * @param lazyModel the lazyModel to set
	 */
	public void setLazyModel(LazyDataModel<Allocation> lazyModel) {
		this.lazyModel = lazyModel;
	}
	
	

}
