package ILocal.entity;

import java.util.List;

public class ResultSearch {
	private List<SearchItem> searchItems;
	private long itemsCount;
	private long pagesCount;

	public List<SearchItem> getSearchItems() {
		return searchItems;
	}

	public void setSearchItems(List<SearchItem> searchItems) {
		this.searchItems = searchItems;
	}

	public long getItemsCount() {
		return itemsCount;
	}

	public void setItemsCount(long itemsCount) {
		itemsCount = itemsCount;
	}

	public long getPagesCount() {
		return pagesCount;
	}

	public void setPagesCount(long pagesCount) {
		this.pagesCount = pagesCount;
	}

	public ResultSearch(List<SearchItem> searchItems, long itemsCount) {
		this.searchItems = searchItems;
		this.itemsCount = itemsCount;
	}

	public ResultSearch(List<SearchItem> searchItems, long itemsCount, long pagesCount) {
		this.searchItems = searchItems;
		this.itemsCount = itemsCount;
		this.pagesCount = pagesCount;
	}
}
