package info.chrismcgee.sky.beans;

import java.sql.Timestamp;

public class PrintType {

	private String id;
	private int sort;
	private String color;
	private String printMethodId;
	private Long defaultMaximum;
	private Timestamp createdAt;
	private Timestamp updatedAt;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getPrintMethodId() {
		return printMethodId;
	}
	public void setPrintMethodId(String printMethodId) {
		this.printMethodId = printMethodId;
	}
	public Long getDefaultMaximum() {
		return defaultMaximum;
	}
	public void setDefaultMaximum(Long defaultMaximum) {
		this.defaultMaximum = defaultMaximum;
	}
	public Timestamp getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}
	public Timestamp getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}
	
}
