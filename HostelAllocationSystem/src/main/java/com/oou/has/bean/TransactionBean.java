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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
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
import com.oou.has.model.Allocation;
import com.oou.has.model.AllocationMethod;
import com.oou.has.model.Constants;
import com.oou.has.lazymodel.TransactionLazyDataModel;
import com.oou.has.model.Transaction;
import com.oou.has.model.Role;
import com.oou.has.model.Room;
import com.oou.has.model.Status;
import com.oou.has.model.User;
import com.oou.has.service.AllocationService;
import com.oou.has.service.PaymentService;
import com.oou.has.service.RoomService;
import com.oou.has.service.TransactionService;
import com.oou.has.service.UserService;

/**
 * @author AAfolayan
 *
 */
@Named("transactionBean")
@ViewScoped
public class TransactionBean implements Serializable {

	public static final String APP_BASE_NAME = Constants.APP_BASE_NAME;
	private static Logger LOG = LoggerFactory.getLogger(TransactionBean.class);
	@Inject
	private UserService userService;
	@Inject
	private TransactionService transactionService;
	@Inject
	RoomService roomService;
	@Inject
	AllocationService allocationService;
	private Transaction entry = new Transaction();
	private LazyDataModel<Transaction> lazyModel;
	private User currentUser;
	private static final String TRANSACTION_MGT_URL = APP_BASE_NAME
			+ "/online/transaction/list.xhtml?faces-redirect=true";
	private static final String TRANSACTION_CREATION_URL = APP_BASE_NAME
			+ "/online/transaction/create.xhtml?faces-redirect=true";
	private static final String DASHBOARD_URL = APP_BASE_NAME
			+ "/online/templates/master.xhtml?faces-redirect=true";

	private List<Transaction> entries = new ArrayList<>();

	private User user = new User();
	private String paymentReference;
	private Transaction transaction;
	@Inject
	PaymentService paymentService;

	@PostConstruct
	public void init() throws IOException {
		LOG.info("TransactionBean init!");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Subject currentUser = SecurityUtils.getSubject();
		this.setCurrentUser(userService.findByUsername(currentUser.getPrincipal().toString()));
		if (facesContext != null && facesContext.getExternalContext() != null) {
			String paymentReferenceFromRequest = facesContext.getExternalContext().getRequestParameterMap()
					.get("paymentReference");

			if (paymentReferenceFromRequest != null) {
				this.paymentReference = paymentReferenceFromRequest;
				System.out.println("Payment reference manually retrieved: " + this.paymentReference);
				this.setTransaction(paymentService.verifyPayment(paymentReferenceFromRequest));
				transactionService.updateTransaction(this.transaction);
				System.out.println("t status" + transaction.getStatus());
				if (this.transaction.getStatus() == Status.SUCCESSFUL) {
					Messages.addFlashGlobalInfo("Transaction successful");
					this.currentUser.setAllocationStatus(Status.APPLIED);
					processAllocation(this.currentUser, this.transaction);
					userService.update(this.currentUser);
				} else {
					Messages.addFlashGlobalError("Transaction failed");

				}
			}
		}

	}

	private void processAllocation(User u,Transaction t) throws IOException {
		Allocation allocation = new Allocation();
		allocation.setAllocationMethod(AllocationMethod.AUTOMATIC);
		allocation.setAllocationTime(t.getCreatedDate());
		allocation.setUserId(u.getId());
	    allocation.setUserName(u.getFullname());
	    allocation.setUserId(u.getId());
		List<Room> availableRooms =roomService.findMostSuitableRoom(t);
		List<Room> preferedRoom = filterRooms(availableRooms);
		allocation.setComments("Automatic Allocation processed successfully");;
		if (preferedRoom.size()<1) {
			allocation.setStatus(Status.FAILED);
			allocation.setComments("no available room");
			Messages.addFlashGlobalError("An error occured durring allocation, please contact admin");
		}
		Random random = new Random();
	    Room selectedRoom = preferedRoom.get(random.nextInt(preferedRoom.size()));
	    allocation.setRoomId(selectedRoom.getId());
	    allocationService.createAllocation(allocation);
	    roomService.updateRoom(selectedRoom,allocation);
	    allocation.setStatus(Status.SUCCESSFUL);
	    u.setAllocationStatus(Status.ALLOCATED);
	    u.setHall(selectedRoom.getHostelName() +  " -->" + "Room"+ selectedRoom.getRoomNumber());
	    userService.update(u);
	    Messages.addFlashGlobalInfo("Congratulations!, your allocation request has been processed successfully");
	    
	    Faces.redirect(DASHBOARD_URL);
	    
	    
		
	}
	private List<Room> filterRooms(List<Room> rooms) {
	    List<Room> rooms2 = new ArrayList<Room>();
	    Set<String> uniqueRoomKeys = new HashSet<>();
	    
	    for (Room r : rooms) {
	        // Create a unique key combining description, size, and amount
	        String uniqueKey = r.getDescription() + "|" + r.getSize() + "|" + r.getAmount();
	        
	        // Only add room if this combination hasn't been seen before
	        if (uniqueRoomKeys.contains(uniqueKey)) {
	            uniqueRoomKeys.add(uniqueKey);
	            rooms2.add(r);
	        }
	    }
	    if (rooms2.size()>1) {
	    
	    return rooms2;
	    }
	    else {
	    	return rooms;
	    }
	}

	public void listTransaction() {
		LOG.info("listTransaction invoked!!!");
		try {
			// SecurityUtils.getSubject().checkRole("MDOPS");
			lazyModel = new TransactionLazyDataModel(transactionService, QueryType.GET_ALL_TRANSACTION);
		} catch (Exception e) {
			Messages.addGlobalError("oops error encountered while fetching entries!");
			LOG.error("oops error encountered while fetching entries!", e.fillInStackTrace());
			e.printStackTrace(); //
		}
	}

	public void listTransaction2() {
		LOG.info("listTransaction invoked!!!");
		try {
			// SecurityUtils.getSubject().checkRole("MDOPS");
			lazyModel = new TransactionLazyDataModel(transactionService, QueryType.GET_ALL_USER_TRANSACTION,this.currentUser.getId());
		} catch (Exception e) {
			Messages.addGlobalError("oops error encountered while fetching entries!");
			LOG.error("oops error encountered while fetching entries!", e.fillInStackTrace());
			e.printStackTrace(); //
		}
	}
	public void refreshTransaction(String ref) {
		this.setTransaction(paymentService.verifyPayment(ref));
	}

	public void updateTransaction() {
		LOG.info("updateTransaction invoked!!!");
		try {
			// SecurityUtils.getSubject().checkRole("MDOPS");
			LOG.info("entry -> " + entry);
			transactionService.updateTransaction(entry);
			Messages.addFlashGlobalInfo("Transaction update request processed successfully!");
			entry = new Transaction();
			Faces.redirect(TRANSACTION_MGT_URL);
		} catch (Exception e) {
			Messages.addGlobalError("Transaction update request failed!");
			LOG.error("Transaction update request failed!", e.fillInStackTrace());
			e.printStackTrace(); // TODO: logger.
			// return null;
		}
	}

	public void createTransaction() {
		LOG.info("createTransaction invoked!!!");
		try {
			// SecurityUtils.getSubject().checkRole("MDOPS");
			LOG.info("entry -> " + entry);
			transactionService.createTransaction(entry);
			Messages.addFlashGlobalInfo("Transaction creation request processed successfully!");
			entry = new Transaction();
			Faces.redirect(TRANSACTION_MGT_URL);
		} catch (Exception e) {
			Messages.addGlobalError("Transaction creation failed!");
			LOG.error("Transaction creation failed!", e.fillInStackTrace());
			e.printStackTrace(); // TODO: logger.
			// return null;
		}
	}

	public void createNewTransactionView() throws IOException {
		LOG.info("createNewTransactionView invoked");
		this.entry = new Transaction();
	}

	public void createTransactionView() throws IOException {
		LOG.info("createTransactionView invoked");
		Faces.redirect(TRANSACTION_CREATION_URL);
	}

	public void displayTransactionDialog(Transaction e) {
		LOG.info("displayTransactionDialog invoked!");
		this.entry = e;
		LOG.info("entry selected:  id -> " + this.entry.getId());

	}

	public void prepare() {
		LOG.info("prepare method invoked!");
		Flash flash = Faces.getFlash();// FacesContext.getCurrentInstance().getExternalContext().getFlash();
		this.entry = (Transaction) flash.get("entry");
		LOG.info("selected Transaction retrieved >>> " + entry);
	}

	private User getCurrentUser() {
		Subject subject = SecurityUtils.getSubject();
		String username = String.valueOf(subject.getPrincipal());
		return userService.findByUsername(username);
	}

	/**
	 * @return the entry
	 */
	public Transaction getEntry() {
		return entry;
	}

	/**
	 * @param entry the entry to set
	 */
	public void setEntry(Transaction entry) {
		this.entry = entry;
	}

	/**
	 * @return the entries
	 */
	public List<Transaction> getEntries() {
		return entries;
	}

	/**
	 * @param entries the entries to set
	 */
	public void setEntries(List<Transaction> entries) {
		this.entries = entries;
	}

	/**
	 * @return the lazyModel
	 */
	public LazyDataModel<Transaction> getLazyModel() {
		return lazyModel;
	}

	/**
	 * @param lazyModel the lazyModel to set
	 */
	public void setLazyModel(LazyDataModel<Transaction> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public String getPaymentReference() {
		return paymentReference;
	}

	public void setPaymentReference(String paymentReference) {
		this.paymentReference = paymentReference;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}

}
