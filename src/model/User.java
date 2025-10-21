package model;

import java.io.Serializable;
import java.sql.Date;

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