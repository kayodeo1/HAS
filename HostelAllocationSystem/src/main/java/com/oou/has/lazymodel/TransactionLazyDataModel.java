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
import com.oou.has.model.Transaction;
import com.oou.has.model.PagedList;
import com.oou.has.model.Status;
import com.oou.has.service.TransactionService;

/**
 * @author AAfolayan
 *
 */
public class TransactionLazyDataModel extends LazyDataModel<Transaction> {

	private TransactionService service;
	private QueryType query;
	private Long userId;

	List<Transaction> list = new ArrayList<>();

	private static Logger LOG = LoggerFactory.getLogger(TransactionLazyDataModel.class);

	public TransactionLazyDataModel(TransactionService service,QueryType query) {
		this.service = service;
		this.query = query;
	}
	public TransactionLazyDataModel(TransactionService service,QueryType query,Long id) {
		this.service = service;
		this.query = query;
		this.userId = id;
	}

	@Override
	public Transaction getRowData(String rowKey) {
		// LOG.info("getRowData method invoked!");
		for (Transaction r : list) {
			if ((r.getId()).equals(rowKey))
				return r;
		}

		return null;
	}

	@Override
	public Object getRowKey(Transaction r) {
		// LOG.info("getRowKey method invoked! " + cdt);
		return r.getId();
	}

	@Override
	public List<Transaction> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		try {
			LOG.info("query invoked >>> " + query);
			List<Transaction> data = new ArrayList<>();
			// paginate db entries
			PagedList<Transaction> pagedList = new PagedList<>();
			switch (query) {
			case GET_ALL_TRANSACTION:
				if (filters.isEmpty()) {
				pagedList = service.fetchTransaction(first, pageSize);
				}else {
					String sortDirection = sortOrder == SortOrder.ASCENDING ? "ASC" : "DESC";
					pagedList = service.fetchTransaction(first, pageSize,sortField,sortDirection, filters);

				}
				break;
			case GET_ALL_USER_TRANSACTION:
				pagedList = service.fetchTransaction(first, pageSize,userId);

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

		} catch (Exception e) {
			LOG.error("oops error encountered while paginating Transaction entries!!!", e);
			e.printStackTrace();
			return new ArrayList<Transaction>();
		}
	}

}
