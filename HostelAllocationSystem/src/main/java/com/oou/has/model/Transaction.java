/**
 * 
 */
package com.oou.has.model;


import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import com.oou.has.model.AbstractEntity;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author AAfolayan
 */
@Entity
public class Transaction extends AbstractEntity {

@Temporal(TemporalType.TIMESTAMP)
private Date createdDate;
@Temporal(TemporalType.TIMESTAMP)
private Date lastModifiedDate;
private Long userId;
private Long allocationId;
private float amount;
private String name;
private String email;
private String paymentMethod;
private String paymentReference;
private String transactionReference;
private String description;
private Status status;
private String currencyCode;
private String roomkey;
private Long hostelId;


/**
 * @return the amount
 */
public float getAmount(){
	return amount;
}
/**
 * @param amount the amount to set
 */
public void setAmount(float amount) {
	this.amount = amount;
}/**
 * @return the paymentReference
 */
public String getPaymentReference(){
	return paymentReference;
}
/**
 * @param paymentReference the paymentReference to set
 */
public void setPaymentReference(String paymentReference) {
	this.paymentReference = paymentReference;
}/**
 * @return the name
 */
public String getName(){
	return name;
}
/**
 * @param name the name to set
 */
public void setName(String name) {
	this.name = name;
}/**
 * @return the paymentMethod
 */
public String getPaymentMethod(){
	return paymentMethod;
}
/**
 * @param paymentMethod the paymentMethod to set
 */
public void setPaymentMethod(String paymentMethod) {
	this.paymentMethod = paymentMethod;
}/**
 * @return the allocationId
 */
public Long getAllocationId(){
	return allocationId;
}
/**
 * @param allocationId the allocationId to set
 */
public void setAllocationId(Long allocationId) {
	this.allocationId = allocationId;
}/**
 * @return the userId
 */
public Long getUserId(){
	return userId;
}
/**
 * @param userId the userId to set
 */
public void setUserId(Long userId) {
	this.userId = userId;
}/**
 * @return the email
 */
public String getEmail(){
	return email;
}
/**
 * @param email the email to set
 */
public void setEmail(String email) {
	this.email = email;
}
@PrePersist
private void onCreate() {
	createdDate = new Date();
}
@PreUpdate
private void onUpdate() {
	lastModifiedDate = new Date();
}
public String getDescription() {
	return description;
}
public void setDescription(String description) {
	this.description = description;
}
public String getCurrencyCode() {
	return currencyCode;
}
public void setCurrencyCode(String currencyCode) {
	this.currencyCode = currencyCode;
}
public Status getStatus() {
	return status;
}
public void setStatus(Status status) {
	this.status = status;
}
public String getTransactionReference() {
	return transactionReference;
}
public void setTransactionReference(String transactionReference) {
	this.transactionReference = transactionReference;
}
/**
 * @return the createdDate
 */
public Date getCreatedDate() {
	return createdDate;
}
/**
 * @param createdDate the createdDate to set
 */
public void setCreatedDate(Date createdDate) {
	this.createdDate = createdDate;
}
/**
 * @return the lastModifiedDate
 */
public Date getLastModifiedDate() {
	return lastModifiedDate;
}
/**
 * @param lastModifiedDate the lastModifiedDate to set
 */
public void setLastModifiedDate(Date lastModifiedDate) {
	this.lastModifiedDate = lastModifiedDate;
}
public String getRoomkey() {
	return roomkey;
}
public void setRoomkey(String roomkey) {
	this.roomkey = roomkey;
}
public Long getHostelId() {
	return hostelId;
}
public void setHostelId(Long hostelId) {
	this.hostelId = hostelId;
}

}