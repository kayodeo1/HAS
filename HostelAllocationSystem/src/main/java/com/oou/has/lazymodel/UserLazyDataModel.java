/**
 *
 */
package com.oou.has.lazymodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oou.has.model.QueryType;
import com.oou.has.model.PagedList;
import com.oou.has.model.Status;
import com.oou.has.model.User;
import com.oou.has.service.UserService;

/**
 * @author AAfolayan
 *
 */
public class UserLazyDataModel extends LazyDataModel<User> {

	private UserService service;
	private Date startDate;
	private Date endDate;
	private Status status;
	private QueryType query;
	private Long connectStore;
	private User manager;

	List<User> list = new ArrayList<>();

	private static Logger LOG = LoggerFactory.getLogger(UserLazyDataModel.class);

	public UserLazyDataModel(UserService service, QueryType query, Status status) {
		this.service = service;
		this.query = query;
		this.status = status;
	}

	public UserLazyDataModel(UserService service, Date startDate, Date endDate, QueryType query) {
		this.service = service;
		this.query = query;
		this.startDate = startDate;
		this.endDate = endDate;
		System.out.println("lazy model set");
	}
	


	@Override
	public User getRowData(String rowKey) {
		// LOG.info("getRowData method invoked!");
		for (User r : list) {
			if ((r.getId()).equals(rowKey)) {
				return r;
			}
		}

		return null;
	}

	@Override
	public Object getRowKey(User r) {
		// LOG.info("getRowKey method invoked! " + cdt);
		return r.getId();
	}

	@Override
	public List<User> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		LOG.info("sort field :"+sortField);
		try {
			LOG.info("query invoked >>> " + query);
			LOG.info("start date >>> " + startDate);
			LOG.info("end date >>> " + endDate);
			LOG.info("status >>> " + status);
			LOG.info("connectStore >>> " + connectStore);
			LOG.info("filters >>> " + filters);
			List<User> data = new ArrayList<>();
			// paginate db entries
			PagedList<User> pagedList = new PagedList<>();
			String sortDirection = sortOrder == SortOrder.ASCENDING ? "ASC" : "DESC";
			switch (query) {
			case GET_ALL_USER:
				if (filters.isEmpty()) {
					LOG.info("filter is empty");
					pagedList = service.fetchUser( first, pageSize);
				} else {
					pagedList=service.fetchUser(first, pageSize,sortField,sortDirection, filters);

				}
				break;
			
			default:
				LOG.warn("query type not found! , " + query);
				break;
			}

			// rowCount
			int dataSize = pagedList.getCount();// data.size();
			this.setRowCount(dataSize);

			// LOG.info("count >>> " + dataSize + " , pagedList.getList() >>> "
			// + pagedList.getList().size());

			return pagedList.getList();

			// paginate
			// if (dataSize > pageSize) {
			// try {
			// return data.subList(first, first + pageSize);
			// } catch (IndexOutOfBoundsException e) {
			// return data.subList(first, first + (dataSize % pageSize));
			// }
			// } else {
			// return data;
			// }
		} catch (

		Exception e) {
			LOG.error("oops error encountered while paginating user entries!!!", e);
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

}
