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
import com.oou.has.model.Hostel;
import com.oou.has.lazymodel.RoomLazyDataModel;
import com.oou.has.model.Room;
import com.oou.has.model.Role;
import com.oou.has.model.User;
import com.oou.has.service.RoomService;
import com.oou.has.service.UserService;

/**
 * @author AAfolayan
 *
 */
@Named("roomBean")
@ViewScoped
public class RoomBean implements Serializable {

    public static final String APP_BASE_NAME = Constants.APP_BASE_NAME;
	private static Logger LOG = LoggerFactory.getLogger(RoomBean.class);
	@Inject
	private UserService userService;
	@Inject
	private RoomService roomService;
	private Room entry = new Room();
	private LazyDataModel<Room> lazyModel;
	private float amount;
	private int number;
	private int roomSize;
	private static final String ROOM_MGT_URL = APP_BASE_NAME + "/online/hostel/roomList.xhtml?faces-redirect=true";
	private static final String ROOM_CREATION_URL = APP_BASE_NAME + "/online/room/create.xhtml?faces-redirect=true";
	private static final String Hostel_CREATION_URL = APP_BASE_NAME + "/online/hostel/hostels.xhtml?faces-redirect=true";
	private Hostel hostel=new Hostel();

	private List<Room> entries = new ArrayList<>();
																		
	
	private User user = new User();

	@PostConstruct
	public void init() throws Exception {
		LOG.info("RoomBean init!");
		Flash flash = Faces.getFlash();
		Hostel hostel = (Hostel) flash.get("hostel");
		flash.clear();
		if (hostel==null) {
			Faces.redirect(Hostel_CREATION_URL);
			return;
		}
		this.setHostel(hostel);
		this.entry.setHostelId(hostel.getId());
		this.entry.setHostelName(this.hostel.getName());
		System.out.println(this.entry.getHostelName());
		this.amount=hostel.getAmount();
		System.out.println("Hostel fetched---->>>>>"+hostel.getName());
	}
	
	public void listRoom() {
		LOG.info("listRoom invoked!!!");
		try {
			//SecurityUtils.getSubject().checkRole("MDOPS");
			lazyModel = new RoomLazyDataModel(roomService, QueryType.GET_HOSTEL_ROOM,hostel.getId());
		} catch (Exception e) {
			Messages.addGlobalError("oops error encountered while fetching entries!");
			LOG.error("oops error encountered while fetching entries!", e.fillInStackTrace());
			e.printStackTrace(); //
		}
	}


	public void updateRoom() {
		LOG.info("updateRoom invoked!!!");
		try {
			//SecurityUtils.getSubject().checkRole("MDOPS");
			LOG.info("entry -> " + entry);
			roomService.updateRoom(entry);
			Messages.addFlashGlobalInfo("Room update request processed successfully!");
			entry = new Room();
			Faces.redirect(ROOM_MGT_URL);
		} catch (Exception e) {
			Messages.addGlobalError("Room update request failed!");
			LOG.error("Room update request failed!", e.fillInStackTrace());
			e.printStackTrace(); // TODO: logger.
			// return null;
		}
	}
	
	
		public void createRoom() {
		LOG.info("createRoom invoked!!!");
		entry.setSize(roomSize);
		if (entry.getSize()==0) {
			Messages.addGlobalError("Room size cannot be zero(0)!");
			return;
		}
		try {
			entry.setAmount(this.amount);
			entry.setType(hostel.getType());
			entry.setHostelId(this.hostel.getId());
			entry.setHostelName(this.hostel.getName());
			LOG.info("entry -> " + entry);
			Room lastRoom=roomService.fetchLastRoom(entry.getHostelId());
			if (lastRoom == null || lastRoom.getRoomNumber() == 0) {
				System.out.println("last room is null");
				lastRoom = new Room();
				lastRoom.setRoomNumber(0);
			}
			System.out.println("last room fetched---->>>>>"+lastRoom.getRoomNumber());
			roomService.createRoom(entry, this.number, lastRoom.getRoomNumber());
			Messages.addFlashGlobalInfo("Room creation request processed successfully!");
			entry = new Room();
			Faces.redirect(ROOM_MGT_URL);
		} catch (Exception e) {
			Messages.addGlobalError("Room creation failed!");
			e.printStackTrace();
			LOG.error("Room creation failed!", e.fillInStackTrace());
			e.printStackTrace(); // TODO: logger.
			// return null;
		}
	}
	
	public void createNewRoomView() throws IOException {
		LOG.info("createNewRoomView invoked");
		this.entry=new Room();
	}
	
  public void createRoomView() throws IOException {
		LOG.info("createRoomView invoked");
		Faces.redirect(ROOM_CREATION_URL);
	}
	
		public void displayRoomDialog(Room e) {
		LOG.info("displayRoomDialog invoked!");
		this.entry = e;
		LOG.info("entry selected:  id -> " + this.entry.getId());

	}
	
		public void prepare() {
		LOG.info("prepare method invoked!");
		Flash flash = Faces.getFlash();// FacesContext.getCurrentInstance().getExternalContext().getFlash();
		this.entry = (Room) flash.get("entry");
		LOG.info("selected Room retrieved >>> " + entry);
	}
	
		private User getCurrentUser() {
		Subject subject = SecurityUtils.getSubject();
		String username = String.valueOf(subject.getPrincipal());
		return userService.findByUsername(username);
	}
	
	
		/**
	 * @return the entry
	 */
	public Room getEntry() {
		return entry;
	}

	/**
	 * @param entry the entry to set
	 */
	public void setEntry(Room entry) {
		this.entry = entry;
	}
	
		/**
	 * @return the entries
	 */
	public List<Room> getEntries() {
		return entries;
	}

	/**
	 * @param entries the entries to set
	 */
	public void setEntries(List<Room> entries) {
		this.entries = entries;
	}
	
		/**
	 * @return the lazyModel
	 */
	public LazyDataModel<Room> getLazyModel() {
		return lazyModel;
	}

	/**
	 * @param lazyModel the lazyModel to set
	 */
	public void setLazyModel(LazyDataModel<Room> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public Hostel getHostel() {
		return hostel;
	}

	public void setHostel(Hostel hostel) {
		this.hostel = hostel;
	}

	public int getRoomSize() {
		return roomSize;
	}

	public void setRoomSize(int roomSize) {
		this.roomSize = roomSize;
	}
	
	

}
