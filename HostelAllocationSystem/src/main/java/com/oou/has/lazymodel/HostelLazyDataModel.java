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
import com.oou.has.model.Hostel;
import com.oou.has.model.PagedList;
import com.oou.has.model.Status;
import com.oou.has.service.HostelService;

/**
 * @author AAfolayan
 *
 */
public class HostelLazyDataModel extends LazyDataModel<Hostel> {

	private HostelService service;
	private QueryType query;

	List<Hostel> list = new ArrayList<>();

	private static Logger LOG = LoggerFactory.getLogger(HostelLazyDataModel.class);

	public HostelLazyDataModel(HostelService service,QueryType query) {
		this.service = service;
		this.query = query;
	}

	@Override
	public Hostel getRowData(String rowKey) {
		// LOG.info("getRowData method invoked!");
		for (Hostel r : list) {
			if ((r.getId()).equals(rowKey))
				return r;
		}

		return null;
	}

	@Override
	public Object getRowKey(Hostel r) {
		// LOG.info("getRowKey method invoked! " + cdt);
		return r.getId();
	}

	@Override
	public List<Hostel> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		try {
			LOG.info("query invoked >>> " + query);
			List<Hostel> data = new ArrayList<>();
			// paginate db entries
			PagedList<Hostel> pagedList = new PagedList<>();
			switch (query) {
			case GET_ALL_HOSTEL:
				pagedList = service.fetchHostel(first, pageSize);
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
			LOG.error("oops error encountered while paginating Hostel entries!!!", e);
			e.printStackTrace();
			return new ArrayList<Hostel>();
		}
	}

}
