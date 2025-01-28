package org.vaadin.addons.componentfactory.demo;

public class Person {

  private String name;
  
  private String note;

  public Person(String name, String note) {
    this.name = name;
    this.note = note;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

}
