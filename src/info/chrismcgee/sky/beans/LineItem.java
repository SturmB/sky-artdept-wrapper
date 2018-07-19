package info.chrismcgee.sky.beans;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class LineItem {
	
	private int id;
	private String orderId;
	private String productNum;
	private String productDetail;
	private String printTypeId;
	private long numImpressions;
	private long impressionsTradition;
	private long impressionsHiSpeed;
	private long impressionsDigital;
	private long quantity;
	private Date itemCompleted;
	private int proofNum;
	private Date proofDate;
	private String thumbnail;
	private int flags;
	private String reorderNum;
	private String packingInstructions;
	private String packageQuantity;
	private String caseQuantity;
	private int labelQuantity;
	private String labelText;
	private String itemStatusId;
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
	public String getProductNum() {
		return productNum;
	}
	public void setProductNum(String productNum) {
		this.productNum = productNum;
	}
	public String getProductDetail() {
		return productDetail;
	}
	public void setProductDetail(String productDetail) {
		this.productDetail = productDetail;
	}
	public String getPrintTypeId() {
		return printTypeId;
	}
	public void setPrintTypeId(String printTypeId) {
		this.printTypeId = printTypeId;
	}
	public long getNumImpressions() {
		return numImpressions;
	}
	public void setNumImpressions(long numImpressions) {
		this.numImpressions = numImpressions;
	}
	public long getQuantity() {
		return quantity;
	}
	public long getImpressionsTradition() {
		return impressionsTradition;
	}
	public void setImpressionsTradition(long impressionsTradition) {
		this.impressionsTradition = impressionsTradition;
	}
	public long getImpressionsHiSpeed() {
		return impressionsHiSpeed;
	}
	public void setImpressionsHiSpeed(long impressionsHiSpeed) {
		this.impressionsHiSpeed = impressionsHiSpeed;
	}
	public long getImpressionsDigital() {
		return impressionsDigital;
	}
	public void setImpressionsDigital(long impressionsDigital) {
		this.impressionsDigital = impressionsDigital;
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
	public String getReorderNum() {
		return reorderNum;
	}
	public void setReorderNum(String reorderNum) {
		this.reorderNum = reorderNum;
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
	public String getItemStatusId() {
		return itemStatusId;
	}
	public void setItemStatusId(String itemStatusId) {
		this.itemStatusId = itemStatusId;
	}
	public List<Artwork> getArtworkList() {
		return artworkList;
	}
	public void setArtworkList(List<Artwork> artworkList) {
		this.artworkList = artworkList;
	}

}
