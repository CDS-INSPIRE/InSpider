package nl.ipo.cds.dao;


public class BaseSearchCriteria {

	private SortOrder sortOrder = SortOrder.ASCENDING;
	private SortField sortField = SortField.BaseSortField.ID;
	private int offset = 0;
	private int limit = -1;

	public BaseSearchCriteria() {
		super();
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

	public SortField getSortField() {
		return sortField;
	}

	public void setSortField(SortField sortField) {
		this.sortField = sortField;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		if (offset < 0) {
			throw new IllegalArgumentException ("Offset must be >= 0");
		}
		
		assert (offset >= 0);
		this.offset = offset;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		if (limit <= 0) {
			throw new IllegalArgumentException ("Limit must be a positive integer");
		}
		
		this.limit = limit;
	}

	public boolean hasLimit() {
		return limit > 0;
	}

}