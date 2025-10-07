package com.oou.has.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;
import org.primefaces.model.LazyDataModel;

import javax.faces.context.Flash;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;

import com.oou.has.lazymodel.UserLazyDataModel;
import com.oou.has.model.Constants;
import com.oou.has.model.Hostel;
import com.oou.has.model.QueryType;
import com.oou.has.model.Role;
import com.oou.has.model.Room;
import com.oou.has.model.Status;
import com.oou.has.model.Transaction;
import com.oou.has.model.Type;
import com.oou.has.model.User;
import com.oou.has.service.HostelService;
import com.oou.has.service.PaymentService;
import com.oou.has.service.RoomService;
import com.oou.has.service.TransactionService;
import com.oou.has.service.UserService;

@Named
@ViewScoped
public class userBean implements java.io.Serializable {
	private User currentUser;
	private User user;

	private String test = "Hello World!";
	private Hostel selectedHostel;
	private Long hostelId;
	private Long selectedRoomId;
	@Inject
	UserService userService;
	@Inject
	TransactionService service;
	@Inject
	PaymentService paymentService;
	@Inject
	HostelService hostelService;
	@Inject
	RoomService roomService;
	private String roomTypeKey = "";
	private String paymentText = "You will be redirected to a secure payment gateway to complete your transaction";
	private LazyDataModel<User> lazyModel;
	public static final String APP_BASE_NAME = Constants.APP_BASE_NAME;

	private static final String USER_LIST_URL = APP_BASE_NAME + "/online/admin/users.xhtml?faces-redirect=true";


	/**
	 * @return the roomTypeKey
	 */
	public String getRoomTypeKey() {
		return roomTypeKey;
	}

	/**
	 * @param roomTypeKey the roomTypeKey to set
	 */
	public void setRoomTypeKey(String roomTypeKey) {
		this.roomTypeKey = roomTypeKey;
	}
	
	public void pullValuesFromFlash(ComponentSystemEvent e) {
		// LOG.info("pullValuesFromFlash method invoked!");
		Flash flash = Faces.getFlash();// FacesContext.getCurrentInstance().getExternalContext().getFlash();
		this.user = (User) flash.get("user");
	}

	@PostConstruct
	public void init() {
		Subject currentUser = SecurityUtils.getSubject();
		this.currentUser = userService.findByUsername(currentUser.getPrincipal().toString());
		try {
			SecurityUtils.getSubject().checkRole("SUPER_ADMIN");
			return;
		} catch (AuthorizationException e) {
			if (this.currentUser.getStatus() == Status.UNVERIFIED) {
				Messages.addGlobalError(
						"Your account is not verified. Please update your profile to apply for hostel allocation.");
			}
		}

	}
	public void searchAll() {
		System.out.println("fetching users.....");
		try {

			setLazyModel(new UserLazyDataModel(userService, null, null, QueryType.GET_ALL_USER));
		} catch (Exception e) {
			e.printStackTrace();
			Messages.addGlobalError("oops error encountered while retrieving entries!");
		}
	}
	public String createUpdateRoleView() {
		// LOG.info("createUpdateRoleView invoked");
		// LOG.info("selected user >>> " + user);
		Flash flash = Faces.getFlash();// FacesContext.getCurrentInstance().getExternalContext().getFlash();
		// flash.setKeepMessages(true);
		flash.put("user", user);
		return "/online/admin/role_update.xhtml?faces-redirect=true";
	}
	public void updateRole() {
		try {
			SecurityUtils.getSubject().checkRole("SUPER_ADMIN");
			System.out.println("user is");
			System.out.println(user.getFirstName());
			userService.updateRole(user);
			
			user.getRoles().clear();
			Flash flash = Faces.getFlash();// FacesContext.getCurrentInstance().getExternalContext().getFlash();
			// flash.put("user", user);
			flash.clear();
			Messages.addFlashGlobalInfo("User profile updated successfully for {0}", user.getFullname());
			user = new User();
			Faces.redirect(USER_LIST_URL);
		} catch (Exception e) {
			Messages.addGlobalError("User update failed!");
			e.printStackTrace(); // TODO: logger.
			// return null;
		}
	}
	public String applyForHostel() {
		if (currentUser.getStatus() == Status.ENABLED) {
			System.out.println("redirecting...");
			return "/online/student/application-form.xhtml?faces-redirect=true";
		} else {
			Messages.addGlobalError(
					"You cannot apply for hostel allocation because your account is not enabled.,Please update your profile to apply for hostel allocation or contact admin.");
			return null;
		}
	}

	public String updateProfileView() {
		return "/online/student/update-profile.xhtml?faces-redirect=true";
	}

	public String getAllocation() {
		if (currentUser.getAllocationStatus() == null) {
			return "Pending Application";
		}
		switch (currentUser.getAllocationStatus()) {

		case NOT_APPLIED: {
			return "Pending Application";
		}
		case IN_PROGRES: {
			return "Application in progress";
		}
		case APPLICATION_FAILED: {
			return "Application Failed , please contact admin";
		}
		default: {
			return currentUser.getHall();
		}
		}

	}

	public void updateUserProfile() throws IOException {
		try {
			this.currentUser.setStatus(Status.ENABLED);
			userService.update(currentUser);
			Messages.addFlashGlobalInfo("Profile updated successfully, you could apply for hostel now");

		} catch (Exception e) {
			this.currentUser.setStatus(Status.UNVERIFIED);
			Messages.addFlashGlobalError("An error occurred while updating your profile, please try again later.");
			e.printStackTrace();
		}
		Faces.redirect("/HostelAllocationSystem/online/templates/master.xhtml");
	}

	public List<Hostel> getAvailableHostels() {
		List<Hostel> hostels = new ArrayList<Hostel>();
		hostels = hostelService.getAvailableHostel(currentUser.getGender());
		return hostels;

	}

	public List<Room> getAvailableRooms() {
		List<Room> rooms = new ArrayList<Room>();
		if (this.selectedHostel == null) {
			return rooms;
		}
		rooms = roomService.getAvailableRooms(this.selectedHostel.getId());
		if (rooms.size() == 0) {
			Messages.addFlashGlobalError("No room available in selected hostel!");
			paymentText="No room available in selected hostel!";
			this.selectedHostel = null;
		}
		return filterRooms(rooms);

	}

	private List<Room> filterRooms(List<Room> rooms) {
		List<Room> rooms2 = new ArrayList<Room>();
		Set<String> uniqueRoomKeys = new HashSet<>();

		for (Room r : rooms) {
			// Create a unique key combining description, size, and amount
			String uniqueKey = r.getDescription() + "|" + r.getSize() + "|" + r.getAmount();

			// Only add room if this combination hasn't been seen before
			if (!uniqueRoomKeys.contains(uniqueKey)) {
				uniqueRoomKeys.add(uniqueKey);
				rooms2.add(r);
			}
		}

		return rooms2;
	}

	public String getSelectedHostelAmount() {
		if (this.selectedHostel == null || this.selectedHostel.getName() == "") {
			return "0";

		}
		return String.valueOf(this.selectedHostel.getAmount());

	}

	public void onHostelChange(AjaxBehaviorEvent e) {
		System.out.println("Hostel selected --->" + hostelId);

		List<Hostel> hostels = getAvailableHostels();
		for (Hostel h : hostels) {
			if (this.hostelId == h.getId()) {
				this.selectedHostel = h;
				System.out.println("selected hostel is --->>>" + this.selectedHostel.getName() + "price is -->"
						+ this.selectedHostel.getAmount());
				return;
			}
		}
	}

	public void onRoomTypeChange() {
		System.out.println("Room selected --->" + selectedRoomId);
		if (selectedRoomId != 0) {
			Room r = roomService.findRoom(selectedRoomId);
			if (r != null) {
				roomTypeKey = r.getDescription() + "|" + r.getSize() + "|" + r.getAmount();
				this.selectedHostel.setAmount(r.getAmount());
				System.out.println(roomTypeKey);
				return;
			}
			System.out.println("room not found");
		}
		System.out.println("random room selected ");

	}

	private String generateTransactionReference() {
		long timestamp = System.currentTimeMillis();
		int random = (int) (Math.random() * 9999);
		return String.format("HAS-%d-%04d", timestamp, random);
	}

	public void processPayment() throws IOException {
		Transaction t = new Transaction();
		t.setAmount(Float.valueOf(this.selectedHostel.getAmount()));
		t.setCurrencyCode("NGN");
		t.setDescription("payment for space in " + this.selectedHostel.getName());
		t.setEmail(this.currentUser.getMatricNumber() + "@gmail.com");
		t.setName(this.currentUser.getFullname());
		t.setPaymentReference(generateTransactionReference());
		t.setStatus(Status.PENDING); // Add this field
		t.setUserId(this.currentUser.getId());
		t.setRoomkey(roomTypeKey);
		t.setHostelId(hostelId);
		service.createTransaction(t);
		Faces.getFlash();
		paymentService.makePayment(t);

	}

	public boolean hasRole(String role) {
		Subject subject = SecurityUtils.getSubject();
		boolean hasRole = subject.hasRole(role);
		// return subject.hasRole(role);
		return hasRole;
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}

	public String getTest() {
		return test;
	}

	public void setTest(String test) {
		this.test = test;
	}

	public Hostel getSelectedHostel() {
		return selectedHostel;
	}

	public void setSelectedHostel(Hostel selectedHostel) {
		this.selectedHostel = selectedHostel;
	}

	public Long getHostelId() {
		return hostelId;
	}

	public void setHostelId(Long hostelId) {
		this.hostelId = hostelId;
	}

	public Long getSelectedRoomId() {
		return selectedRoomId;
	}

	public void setSelectedRoomId(Long selectedRoomId) {
		this.selectedRoomId = selectedRoomId;
	}

	public String getPaymentText() {
		return paymentText;
	}

	public void setPaymentText(String paymentText) {
		this.paymentText = paymentText;
	}

	public LazyDataModel<User> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<User> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
