/**
 *
 */
package com.oou.has.model;

import java.util.Date;
/**
 * @author Afolayana
 *
 */
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
//@NamedQueries({
//		@NamedQuery(name = "User.find", query = "SELECT u FROM User u WHERE u.username = :matricNumber AND u.password = :password"),
//		@NamedQuery(name = "User.list", query = "SELECT u FROM User u where u.status=:status") })
// @Table(name = "user", schema = "public2")
@Table(name = "USER_")
public class User extends AbstractEntity {

	// @Id
	// @GeneratedValue(generator = "user_id_seq", strategy =
	// GenerationType.SEQUENCE)
	// @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq",
	// schema = "public2", allocationSize = 1)
	// @Column(name = "id")
	// private Long id;

	@NotNull
	@Column(unique = true, name = "matricNumber")
	private String matricNumber;
	private String firstName;
	private String lastName;
	private String department;
	private String fullname;
	private String gender;
	private int level;
	private Status allocationStatus = Status.NOT_APPLIED;

	private String hall;

	private String role;


	// @NotNull
	@Column(name = "password")
	private String password;
	// private String firstName;
	// private String lastName;
	@Enumerated(EnumType.STRING)
	private Status status;

	@ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
	@Enumerated(EnumType.STRING)
	// @CollectionTable(name = "userroles", joinColumns = { @JoinColumn(name =
	// "userid") }, schema = "public2")
	@CollectionTable(name = "userroles", joinColumns = { @JoinColumn(name = "userid") })
	@Column(name = "role")
	private List<Role> roles;
	
	@Transient
	private String initials;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastModifiedDate;




	@PrePersist
	private void onCreate() {
		fullname = firstName +" " +lastName;
		createdDate = new Date();
	}

	@PreUpdate
	private void onUpdate() {
		lastModifiedDate = new Date();
		fullname = firstName +" " +lastName;

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

	/**
	 * @return the status
	 */
	@Enumerated(EnumType.STRING)
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Status status) {
		this.status = status;
	}


	public void setMatricNumber(String matricNumber) {
		this.matricNumber = matricNumber;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the matricNumber
	 */
	public String getMatricNumber() {
		return matricNumber;
	}





	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the roles
	 */
	public List<Role> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.id + "$" + this.matricNumber;
	}


	/**
	 * @return the department
	 */
	public String getDepartment() {
		return department;
	}

	/**
	 * @param department the department to set
	 */
	public void setDepartment(String department) {
		this.department = department;
	}



	/**
	 * @return the role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(String role) {
		this.role = role;
	}

	public static String extractInitials(String fullName) {
		String[] words = fullName.split("\\s+");
		StringBuilder initials = new StringBuilder();
		int count = 0;

		for (String word : words) {
			if (word.length() > 0 && !isTitleWord(word) && count < 2) {
				initials.append(Character.toUpperCase(word.charAt(0)));
				count++;
			}
		}

		return initials.toString();
	}

	private static boolean isTitleWord(String word) {
		String lowerWord = word.toLowerCase();
		return lowerWord.equals("mr") || lowerWord.equals("mrs") || lowerWord.equals("ms") || lowerWord.equals("dr")
				|| lowerWord.equals("prof");
	}

	/**
	 * @return the initials
	 */
	public String getInitials() {
		return extractInitials(this.firstName+this.lastName);
	}

	/**
	 * @param initials the initials to set
	 */
	public void setInitials(String initials) {
		this.initials = initials;
	}

	public String getHall() {
		return hall;
	}

	public void setHall(String hall) {
		this.hall = hall;
	}

	public String getFullname() {
		return this.firstName +" " + this.lastName;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Status getAllocationStatus() {
		return allocationStatus;
	}

	public void setAllocationStatus(Status allocationStatus) {
		this.allocationStatus = allocationStatus;
	}

}