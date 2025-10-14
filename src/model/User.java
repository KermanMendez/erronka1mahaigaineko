package model;

import java.sql.Date;
//import java.util.Date;

public class User {
	
	private String id;
	private String name;
	private String surname;
	private String surname2;
	private String email;
	private String password;
	private Date birthDate;
	
	public User(String id, String name, String surname, String surname2, String email, String password,
			Date birthDate) {
		this.id = id;
		this.name = name;
		this.surname = surname;
		this.surname2 = surname2;
		this.email = email;
		this.password = password;
		this.birthDate = birthDate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getSurname2() {
		return surname2;
	}

	public void setSurname2(String surname2) {
		this.surname2 = surname2;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}	

}
