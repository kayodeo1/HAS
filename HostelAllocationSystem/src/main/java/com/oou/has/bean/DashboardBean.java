package com.oou.has.bean;

import java.io.Serializable;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.oou.has.service.AllocationService;
import com.oou.has.service.HostelService;
import com.oou.has.service.RoomService;
import com.oou.has.service.TransactionService;
import com.oou.has.service.UserService;

@Named
@ViewScoped
public class DashboardBean implements Serializable{
	private static final long serialVersionUID = 1L;
	private HashMap<String, Object> stats ;
	@Inject
	HostelService hostelService;
	@Inject
	RoomService roomService;
	@Inject
	AllocationService allocationService;
	@Inject
	UserService userService;
	@Inject
	TransactionService transactionService;
	
	
	
	@PostConstruct
	public void init() {
		
	}
	
	public void loadStats() {
		stats = new HashMap<String, Object>();
		stats.put("hostelCount", hostelService.fetchHostelCount().intValue());
		stats.put("roomCount", roomService.fetchRoomCount().intValue());
		stats.put("availableRoomCount", roomService.fetchAvailableRoomCount().intValue());
		stats.put("unavailableRoomCount",roomService.fetchUnavailableRoomCount().intValue() );
		stats.put("allocationCount",allocationService.fetchAllocationCount().intValue() );
		try {
			stats.put("userCount",userService.fetchUserCount().intValue() );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stats.put("amountReceived",transactionService.fetchTotalAmount() );
		stats.put("failedTransactiionCount",transactionService.fetchFailedransactionCount().intValue() );




	}



	public HashMap<String, Object> getStats() {
		return stats;
	}



	public void setStats(HashMap<String, Object> stats) {
		this.stats = stats;
	}

}
