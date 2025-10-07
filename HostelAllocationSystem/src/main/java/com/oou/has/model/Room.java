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
import javax.persistence.Transient;

import com.oou.has.model.AbstractEntity;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author AAfolayan
 */
@Entity
public class Room extends AbstractEntity {

@Temporal(TemporalType.TIMESTAMP)
private Date createdDate;
@Temporal(TemporalType.TIMESTAMP)
private Date lastModifiedDate;
private Long hostelId;
private Long creatorId;
private float amount;
private boolean isFull;
@Transient
private List<User> occupants;
private int size;
private String description;
private String hostelName;
private int roomNumber;
private Type type;


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
/**
 * @return the hostelName
 */
public String getHostelName() {
	return hostelName;
}
/**
 * @param hostelName the hostelName to set
 */
public void setHostelName(String hostelName) {
	this.hostelName = hostelName;
}
/**
 * @return the roomNumber
 */
public int getRoomNumber() {
	return roomNumber;
}
/**
 * @param roomNumber the roomNumber to set
 */
public void setRoomNumber(int roomNumber) {
	this.roomNumber = roomNumber;
}
/**
 * @param isFull the isFull to set
 */
public void setFull(boolean isFull) {
	this.isFull = isFull;
}
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
 * @return the size
 */
public int getSize(){
	return size;
}
/**
 * @param size the size to set
 */
public void setSize(int size) {
	this.size = size;
}/**
 * @return the hostelId
 */
public Long getHostelId(){
	return hostelId;
}
/**
 * @param hostelId the hostelId to set
 */
public void setHostelId(Long hostelId) {
	this.hostelId = hostelId;
}/**
 * @return the creatorId
 */
public Long getCreatorId(){
	return creatorId;
}
/**
 * @param creatorId the creatorId to set
 */
public void setCreatorId(Long creatorId) {
	this.creatorId = creatorId;
}/**
 * @return the description
 */
public String getDescription(){
	return description;
}
/**
 * @param description the description to set
 */
public void setDescription(String description) {
	this.description = description;
}/**
 * @return the isFull
 */
public boolean getIsFull(){
	return isFull;
}
/**
 * @param isFull the isFull to set
 */
public void setIsFull(boolean isFull) {
	this.isFull = isFull;
}/**
 * @return the occupants
 */
public List<User> getOccupants(){
	return occupants;
}
/**
 * @param occupants the occupants to set
 */
public void setOccupants(List<User> occupants) {
	this.occupants = occupants;
}
@PrePersist
private void onCreate() {
	createdDate = new Date();
}
@PreUpdate
private void onUpdate() {
	lastModifiedDate = new Date();
	if (getOccupants().size()==getSize()) {
		setIsFull(true);
	}else {
		setIsFull(false);
	}
}
public Type getType() {
	return type;
}
public void setType(Type type) {
	this.type = type;
}

}