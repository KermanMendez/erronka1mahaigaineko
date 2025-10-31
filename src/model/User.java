package model;

import java.io.Serializable;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String name;
	private String surname;
	private String surname2;
	private String email;
	private String password;
	private Date birthDate;
	private boolean isTrainer;

	public User(String id, String name, String surname, String surname2, String email, String password, Date birthDate,
			boolean isTrainer) {
		this.id = id;
		this.name = name;
		this.surname = surname;
		this.surname2 = surname2;
		this.email = email;
		this.password = password;
		this.birthDate = birthDate;
		this.isTrainer = isTrainer;
	}

	public User(String username, String email, String password) {
		this.name = username;
		this.email = email;
		this.password = password;
	}

	/**
	 * Convenience constructor used by EditProfile.validateChanges.
	 * birthDateStr expected in dd/MM/yyyy format; if invalid, birthDate will be null.
	 */
	public User(String name, String surname, String surname2, String password, String birthDateStr) {
		this.name = name;
		this.surname = surname;
		this.surname2 = surname2;
		this.password = password;
		if (birthDateStr != null && !birthDateStr.trim().isEmpty()) {
			try {
				java.util.Date parsed = new SimpleDateFormat("dd/MM/yyyy").parse(birthDateStr);
				this.birthDate = new java.sql.Date(parsed.getTime());
			} catch (ParseException e) {
				this.birthDate = null;
			}
		} else {
			this.birthDate = null;
		}
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public String getEmail() {
		return email;
	}

	public String getId() {
		return id;
	}

	public boolean isTrainer() {
		return isTrainer;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public String getSurname() {
		return surname;
	}

	public String getSurname2() {
		return surname2;
	}

	public String getFullSurname() {
		String s1 = surname != null ? surname : "";
		String s2 = surname2 != null ? surname2 : "";
		return (s1 + (s2.isEmpty() ? "" : " " + s2)).trim();
	}

	public String getDobString() {
		if (birthDate == null)
			return "";
		return new SimpleDateFormat("dd/MM/yyyy").format(birthDate);
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public void setSurname2(String surname2) {
		this.surname2 = surname2;
	}

	public void setTrainer(boolean isTrainer) {
		this.isTrainer = isTrainer;
	}
}