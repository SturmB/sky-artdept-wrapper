package info.chrismcgee.sky.beans;

import info.chrismcgee.sky.enums.PrintingCompany;

import java.sql.Date;
import java.util.List;

public class Order {

	private Date shipDateId;
	private String id;
	private String customerName;
	private String customerPO;
	private Date proofSpecDate;
	private Date jobCompleted;
	private PrintingCompany printingCompany;
	private boolean overruns;
	private boolean sampleShelfNote;
	private String sigProof;
	private String sigOutput;
	private boolean rush;
	private List<LineItem> lineItemList;
	
	public Date getShipDateId() {
		return shipDateId;
	}
	public void setShipDateId(Date date) {
		this.shipDateId = date;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getCustomerPO() {
		return customerPO;
	}
	public void setCustomerPO(String customerPO) {
		this.customerPO = customerPO;
	}
	public Date getProofSpecDate() {
		return proofSpecDate;
	}
	public void setProofSpecDate(Date specDate) {
		this.proofSpecDate = specDate;
	}
	public Date getJobCompleted() {
		return jobCompleted;
	}
	public void setJobCompleted(Date jobCompleted) {
		this.jobCompleted = jobCompleted;
	}
	public PrintingCompany getPrintingCompany() {
		return printingCompany;
	}
	public void setPrintingCompany(PrintingCompany printingCompany) {
		this.printingCompany = printingCompany;
	}
	public boolean isOverruns() {
		return overruns;
	}
	public void setOverruns(boolean overruns) {
		this.overruns = overruns;
	}
	public boolean isSampleShelfNote() {
		return sampleShelfNote;
	}
	public void setSampleShelfNote(boolean sampleShelfNote) {
		this.sampleShelfNote = sampleShelfNote;
	}
	public String getSigProof() {
		return sigProof;
	}
	public void setSigProof(String sigProof) {
		this.sigProof = sigProof;
	}
	public String getSigOutput() {
		return sigOutput;
	}
	public void setSigOutput(String sigOutput) {
		this.sigOutput = sigOutput;
	}
	public boolean isRush() {
		return rush;
	}
	public void setRush(boolean rush) {
		this.rush = rush;
	}
	public List<LineItem> getLineItemList() {
		return lineItemList;
	}
	public void setLineItemList(List<LineItem> lineItemList) {
		this.lineItemList = lineItemList;
	}
	
}
