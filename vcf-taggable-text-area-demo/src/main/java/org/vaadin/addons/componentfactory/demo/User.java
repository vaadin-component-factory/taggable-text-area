package org.vaadin.addons.componentfactory.demo;

import java.time.LocalDate;

public class User {
	
	private String name;
	private String email;
	private String notes;
	private LocalDate birthDate;
	
	public User(String name, String email, String notes, LocalDate birthDate) {
        this.name = name;
        this.email = email;
        this.notes = notes;
        this.birthDate = birthDate;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String toString() {
		return name;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

}
