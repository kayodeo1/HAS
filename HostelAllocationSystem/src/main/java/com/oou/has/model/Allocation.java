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
public class Allocation extends AbstractEntity {

@Temporal(TemporalType.TIMESTAMP)
private Date createdDate;
@Temporal(TemporalType.TIMESTAMP)
private Date lastModifiedDate;
private Long userId;
private String userName;
private Long roomId;
@Enumerated(EnumType.STRING)
private Status status;
@Enumerated(EnumType.STRING)
private AllocationMethod allocationMethod;
private Date allocationTime;
private Date modifiedDate;
private String comments;


/**
 * @return the createdDate
 */
public Date getCreatedDate(){
	return createdDate;
}
/**
 * @param createdDate the createdDate to set
 */
public void setCreatedDate(Date createdDate) {
	this.createdDate = createdDate;
}/**
 * @return the allocationMethod
 */
public AllocationMethod getAllocationMethod(){
	return allocationMethod;
}
/**
 * @param allocationMethod the allocationMethod to set
 */
public void setAllocationMethod(AllocationMethod allocationMethod) {
	this.allocationMethod = allocationMethod;
}/**
 * @return the modifiedDate
 */
public Date getModifiedDate(){
	return modifiedDate;
}
/**
 * @param modifiedDate the modifiedDate to set
 */
public void setModifiedDate(Date modifiedDate) {
	this.modifiedDate = modifiedDate;
}/**
 * @return the allocationTime
 */
public Date getAllocationTime(){
	return allocationTime;
}
/**
 * @param allocationTime the allocationTime to set
 */
public void setAllocationTime(Date allocationTime) {
	this.allocationTime = allocationTime;
}/**
 * @return the userName
 */
public String getUserName(){
	return userName;
}
/**
 * @param userName the userName to set
 */
public void setUserName(String userName) {
	this.userName = userName;
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
 * @return the roomId
 */
public Long getRoomId(){
	return roomId;
}
/**
 * @param roomId the roomId to set
 */
public void setRoomId(Long roomId) {
	this.roomId = roomId;
}/**
 * @return the status
 */
public Status getStatus(){
	return status;
}
/**
 * @param status the status to set
 */
public void setStatus(Status status) {
	this.status = status;
}
@PrePersist
private void onCreate() {
	createdDate = new Date();
}
@PreUpdate
private void onUpdate() {
	lastModifiedDate = new Date();
}
public String getComments() {
	return comments;
}
public void setComments(String comments) {
	this.comments = comments;
}

}