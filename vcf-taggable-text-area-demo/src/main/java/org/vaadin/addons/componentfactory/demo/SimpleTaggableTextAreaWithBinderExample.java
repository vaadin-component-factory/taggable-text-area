package org.vaadin.addons.componentfactory.demo;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import java.time.LocalDate;
import java.util.Arrays;
import org.vaadin.addons.componentfactory.tta.TaggableTextArea;

@SuppressWarnings("serial")
@Route("simple-test-binder")
public class SimpleTaggableTextAreaWithBinderExample extends VerticalLayout {
  
  private Button editTextAreaButton = new Button("Edit Text Area");
  
  public SimpleTaggableTextAreaWithBinderExample() {
    
    Person person = new Person("Tom Smith", "Note for John Doe. Joining Jo on upcoming event with Jane D.");

    // binder
    Binder<Person> binder = new Binder<>(Person.class);

    // some text field
    TextField nameField = new TextField("User name");
    binder.bind(nameField, Person::getName, Person::setName);
    nameField.setReadOnly(true);

    // taggable text area field
    TaggableTextArea<User> textArea = createBasicTaggableTextArea();
    binder.forField(textArea).bind("note");
    textArea.setEnabled(false);
    
    binder.setBean(person);
     
    Button saveButton = new Button("Save",
        event -> {
          Notification.show("User being saved! " + binder.getBean().getNote());
          // A real application would also save
          // the updated person using the application's backend
          editTextAreaButton.setEnabled(true);
          textArea.setEnabled(false);
    });
    saveButton.setEnabled(false);
    saveButton.setDisableOnClick(true);
    
    editTextAreaButton.addClickListener(e -> {
      Notification.show("Edit Text Area - Current value: " + binder.getBean().getNote());
      textArea.setEnabled(true);
    });
    editTextAreaButton.setDisableOnClick(true);
       
    textArea.addValueChangeListener(e -> {
       saveButton.setEnabled(true);
    });
    
    add(nameField, textArea, new HorizontalLayout(editTextAreaButton, saveButton));
  }

  private TaggableTextArea<User> createBasicTaggableTextArea() {
    TaggableTextArea<User> tta = new TaggableTextArea<User>(
        Arrays.asList(new User("John Doe", "jdoe@example.com", "", LocalDate.now(), ""),
            new User("Jane Doe", "jane@example.com", "", LocalDate.now(), ""),
            new User("Jo", "jo@example.com", "", LocalDate.now(),""),
            new User("Jane D", "janed@example.com", "", LocalDate.now(), ""))) {

      @Override
      protected Component createTagPopupContent(User relatedItem) {
        return new Button("Show user information", ev -> {
          Notification
              .show("User: " + relatedItem.getName() + ", Email: " + relatedItem.getEmail());
        });
      }

    };
    tta.setWidth("200px");
    tta.setClassName("custom-taggable-text-area");
    return tta;
  }

}
