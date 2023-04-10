package com.vaadin.componentfactory.demo;

import java.time.LocalDate;

public class Person {
	
	private String firstName;
    private String lastName;
    private String phone;
    private LocalDate birthday;

    public Person() {}

    public Person(String firstName, String lastName, String phone){
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }
    
    public Person(String firstName, String lastName, String phone, LocalDate birthday){
        this(firstName, lastName, phone);
        this.birthday = birthday;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

	public LocalDate getBirthday() {
		return birthday;
	}

	public void setBirthday(LocalDate birthday) {
		this.birthday = birthday;
	}
    
}
