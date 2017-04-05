package info.chrismcgee.sky.beans;

import info.chrismcgee.sky.enums.PrintType;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class OrderDetail {
	
	private int id;
	private String orderId;
	private String productId;
	private String productDetail;
	private PrintType printType;
	private long numColors;
	private long quantity;
	private Date itemCompleted;
	private int proofNum;
	private Date proofDate;
	private String thumbnail;
	private int flags;
	private String reorderId;
	private String packingInstructions;
	private String packageQuantity;
	private String caseQuantity;
	private int labelQuantity;
	private String labelText;
	private String digitalFilename;
	private List<Artwork> artworkList = new ArrayList<Artwork>();
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getProductDetail() {
		return productDetail;
	}
	public void setProductDetail(String productDetail) {
		this.productDetail = productDetail;
	}
	public PrintType getPrintType() {
		return printType;
	}
	public void setPrintType(PrintType printType) {
		this.printType = printType;
	}
	public long getNumColors() {
		return numColors;
	}
	public void setNumColors(long numColors) {
		this.numColors = numColors;
	}
	public long getQuantity() {
		return quantity;
	}
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}
	public Date getItemCompleted() {
		return itemCompleted;
	}
	public void setItemCompleted(Date itemCompleted) {
		this.itemCompleted = itemCompleted;
	}
	public int getProofNum() {
		return proofNum;
	}
	public void setProofNum(int proofNum) {
		this.proofNum = proofNum;
	}
	public Date getProofDate() {
		return proofDate;
	}
	public void setProofDate(Date proofDate) {
		this.proofDate = proofDate;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	public int getFlags() {
		return flags;
	}
	public void setFlags(int flags) {
		this.flags = flags;
	}
	public String getReorderId() {
		return reorderId;
	}
	public void setReorderId(String reorderId) {
		this.reorderId = reorderId;
	}
	public String getPackingInstructions() {
		return packingInstructions;
	}
	public void setPackingInstructions(String packingInstructions) {
		this.packingInstructions = packingInstructions;
	}
	public String getPackageQuantity() {
		return packageQuantity;
	}
	public void setPackageQuantity(String packageQuantity) {
		this.packageQuantity = packageQuantity;
	}
	public String getCaseQuantity() {
		return caseQuantity;
	}
	public void setCaseQuantity(String caseQuantity) {
		this.caseQuantity = caseQuantity;
	}
	public int getLabelQuantity() {
		return labelQuantity;
	}
	public void setLabelQuantity(int labelQuantity) {
		this.labelQuantity = labelQuantity;
	}
	public String getLabelText() {
		return labelText;
	}
	public void setLabelText(String labelText) {
		this.labelText = labelText;
	}
	public String getDigitalFilename() {
		return digitalFilename;
	}
	public void setDigitalFilename(String digitalFilename) {
		this.digitalFilename = digitalFilename;
	}
	public List<Artwork> getArtworkList() {
		return artworkList;
	}
	public void setArtworkList(List<Artwork> artworkList) {
		this.artworkList = artworkList;
	}

}
