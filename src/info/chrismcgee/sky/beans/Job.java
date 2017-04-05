package info.chrismcgee.sky.beans;

import info.chrismcgee.sky.enums.PrintingCompany;

import java.sql.Date;
import java.util.List;

public class Job {

	private Date shipDate;
	private String jobId;
	private String customerName;
	private String customerPO;
//	private Timestamp proofSpecDate;
	private Date proofSpecDate;
//	private Timestamp jobCompleted;
	private Date jobCompleted;
	private PrintingCompany printingCompany;
	private boolean overruns;
	private boolean sampleShelfNote;
	private String sigProof;
	private String sigOutput;
	private List<OrderDetail> orderDetailList;
	
	public Date getShipDate() {
		return shipDate;
	}
	public void setShipDate(Date date) {
		this.shipDate = date;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
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
	public List<OrderDetail> getOrderDetailList() {
		return orderDetailList;
	}
	public void setOrderDetailList(List<OrderDetail> orderDetailList) {
		this.orderDetailList = orderDetailList;
	}
	
}
