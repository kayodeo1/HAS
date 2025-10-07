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
import com.oou.has.model.Allocation;
import com.oou.has.model.PagedList;
import com.oou.has.model.Status;
import com.oou.has.service.AllocationService;

/**
 * @author AAfolayan
 *
 */
public class AllocationLazyDataModel extends LazyDataModel<Allocation> {

	private AllocationService service;
	private QueryType query;

	List<Allocation> list = new ArrayList<>();

	private static Logger LOG = LoggerFactory.getLogger(AllocationLazyDataModel.class);

	public AllocationLazyDataModel(AllocationService service,QueryType query) {
		this.service = service;
		this.query = query;
	}

	@Override
	public Allocation getRowData(String rowKey) {
		// LOG.info("getRowData method invoked!");
		for (Allocation r : list) {
			if ((r.getId()).equals(rowKey))
				return r;
		}

		return null;
	}

	@Override
	public Object getRowKey(Allocation r) {
		// LOG.info("getRowKey method invoked! " + cdt);
		return r.getId();
	}

	@Override
	public List<Allocation> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		try {
			LOG.info("query invoked >>> " + query);
			List<Allocation> data = new ArrayList<>();
			// paginate db entries
			PagedList<Allocation> pagedList = new PagedList<>();
			switch (query) {
			case GET_ALL_ALLOCATION:
				if (filters.isEmpty()) {
				pagedList = service.fetchAllocation(first, pageSize);
				}else {
					System.out.println("filter searching");

					String sortDirection = sortOrder == SortOrder.ASCENDING ? "ASC" : "DESC";
					pagedList = service.fetchAllocation(first, pageSize,sortField,sortDirection, filters);
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

		} catch (Exception e) {
			LOG.error("oops error encountered while paginating Allocation entries!!!", e);
			e.printStackTrace();
			return new ArrayList<Allocation>();
		}
	}

}
