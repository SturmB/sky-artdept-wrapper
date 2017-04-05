package info.chrismcgee.sky.beans;

public class Artwork {

	private int id;
	private int orderDetailId;
	private String digitalArtFile;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getOrderDetailId() {
		return orderDetailId;
	}
	public void setOrderDetailId(int orderDetailId) {
		this.orderDetailId = orderDetailId;
	}
	public String getDigitalArtFile() {
		return digitalArtFile;
	}
	public void setDigitalArtFile(String digitalArtFile) {
		this.digitalArtFile = digitalArtFile;
	}
	
}
