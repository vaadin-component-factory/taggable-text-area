/*-
 * #%L
 * Taggable Text Area
 * %%
 * Copyright (C) 2024 Vaadin Ltd
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.vaadin.addons.componentfactory.demo;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.vaadin.addons.componentfactory.tta.FilterListSelector;
import org.vaadin.addons.componentfactory.tta.TaggableTextArea;

/**
 * View for {@link TaggableTextArea} demo.
 *
 * @author Vaadin Ltd
 */
@SuppressWarnings("serial")
@Route("")
public class TaggableTextAreaDemoView extends DemoView {

  @Override
  public void initView() {
    createBasicTaggableTextAreaDemo();
    createGridDemo();

    addCard("Additional code used in the demo", new Span("These methods are used in the demo."));
  }

  @SuppressWarnings("rawtypes")
  private void createGridDemo() {
    
    // begin-source-example
    // source-example-heading: TaggableTextArea used on Grid
    Grid<User> usersGrid = new Grid<>();
    usersGrid.setItems(Arrays.asList(
        new User("John Doe", "jdoe@example.com", "", LocalDate.now(),
            "https://randomuser.me/api/portraits/men/77.jpg"),
        new User("Jane Doe", "jane@example.com", "", LocalDate.now(),
            "https://randomuser.me/api/portraits/women/43.jpg")));

    Editor<User> editor = usersGrid.getEditor();
    Grid.Column<User> nameColumn = usersGrid.addColumn(User::getName).setHeader("Name");
    Grid.Column<User> emailColumn = usersGrid.addColumn(User::getEmail).setHeader("Email");
    Grid.Column<User> notesColumn = usersGrid.addColumn(new ComponentRenderer<>(user -> {
      Span span = new Span();
      span.getElement().executeJs("this.innerHTML = $0", user.getNotes());
      return span;
    })).setHeader("Notes").setResizable(true);
    Grid.Column<User> birthDateColumn =
        usersGrid.addColumn(User::getBirthDate).setHeader("Birthdate");
    Grid.Column<User> editColumn = usersGrid.addComponentColumn(person -> {
      Button editButton = new Button("Edit");
      editButton.addClickListener(e -> {
        if (editor.isOpen())
          editor.cancel();
        usersGrid.getEditor().editItem(person);
      });
      return editButton;
    }).setWidth("150px").setFlexGrow(0);
    Binder<User> binder = new Binder<>();
    editor.setBinder(binder);

    TextField nameField = new TextField();
    nameField.setWidthFull();
    binder.forField(nameField).asRequired("Name must not be empty").bind(User::getName,
        User::setName);
    nameColumn.setEditorComponent(nameField);

    TextField emailField = new TextField();
    emailField.setWidthFull();
    binder.forField(emailField).asRequired("Email must not be empty").bind(User::getEmail,
        User::setEmail);
    emailColumn.setEditorComponent(emailField);

    TaggableTextArea<User> notesField = createGridTaggableTextArea();
    notesField.setWidthFull();
    binder.forField(notesField).bind(User::getNotes, User::setNotes);
    notesColumn.setEditorComponent(notesField);
    notesField.setHeight("100px");

    DatePicker birthDateField = new DatePicker();
    birthDateField.setWidthFull();
    binder.forField(birthDateField).bind(User::getBirthDate, User::setBirthDate);
    birthDateColumn.setEditorComponent(birthDateField);

    Button saveButton = new Button("Save", e -> {
      editor.save();
      editor.closeEditor();
      usersGrid.getDataCommunicator().refresh(editor.getBinder().getBean());
    });
    Button cancelButton = new Button(VaadinIcon.CLOSE.create(), e -> editor.cancel());
    cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
    HorizontalLayout actions = new HorizontalLayout(saveButton, cancelButton);
    actions.setPadding(false);
    editColumn.setEditorComponent(actions);

    usersGrid.addItemDoubleClickListener(e -> {
      editor.editItem(e.getItem());
      Component editorComponent = e.getColumn().getEditorComponent();
      if (editorComponent instanceof Focusable) {
        ((Focusable) editorComponent).focus();
      }
    });
    // end-source-example
    
    addCard("TaggableTextArea used on Grid", usersGrid);
  }

  private void createBasicTaggableTextAreaDemo() {
    Div message = createMessageDiv("simple-taggable-text-area-demo-message");
    Div plainMessage = createMessageDiv("simple-taggable-text-area-demo-plain-message");

    // begin-source-example
    // source-example-heading: Simple example for TaggableTextArea
    TaggableTextArea<User> tta = createBasicTaggableTextArea();
    tta.setHeight("200px");
    tta.setLabel("Enter text, use @ to tag user");
    tta.addValueChangeListener(ev -> {
      updateMessage(message, "<b>Text Area Value:</b> <br>" + tta.getValue());
      updateMessage(plainMessage, "<b>Text Area HTML Value:</b> <br>" + tta.getHtmlValue());
    });
    tta.setValue(
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec rutrum diam sed sem interdum semper. "
            + "<div>Morbi velit mi, interdum eu luctus sit amet, imperdiet et nisi. Praesent odio metus, porttitor mattis diam eu, </div>"
            + "<div>mattis rutrum lectus. John Doe. Pellentesque elementum non urna vitae commodo. Praesent finibus nunc purus, eget </div>"
            + "<div>facilisis tortor sollicitudin id. Etiam accumsan nisl mauris, eu semper nisl convallis facilisis. Jane Doe.</div>"
            + "<div>Sed varius volutpat aliquam. Suspendisse augue augue, maximus fermentum sagittis varius, commodo </div>"
            + "<div>id eros. Nunc vulputate justo vel sagittis ultrices. Quisque vehicula lorem in orci elementum interdum. </div>"
            + "<div>In in sem quis dolor convallis aliquet sed fermentum mi. Jane Doe </div>");
    tta.setTagPopupFor(item -> item.getName().equals("John Doe"));

    showUsedTags(tta.obtainUsedTags(), tta);
    Checkbox cb = new Checkbox("Readonly");
    cb.addValueChangeListener(ev -> tta.setReadOnly(cb.getValue()));
    Button showUsedTags = new Button("Show used tags", ev -> showUsedTags(tta.obtainUsedTags(),tta));
    // end-source-example

    addCard("Simple example for TaggableTextArea", tta, message, plainMessage,
        new HorizontalLayout(showUsedTags, cb));
  }

  // begin-source-example
  // source-example-heading: Additional code used in the demo
  /**
   * Additional code used in the demo
   */
  private void updateMessage(Div message, String value) {
    message.getElement().setProperty("innerHTML", value);
  }

  private Div createMessageDiv(String id) {
    Div message = new Div();
    message.setId(id);
    message.getStyle().set("whiteSpace", "normal");
    return message;
  }

  private TaggableTextArea<User> createBasicTaggableTextArea() {
    TaggableTextArea<User> tta = new TaggableTextArea<User>(Arrays.asList(
        new User("John Doe", "jdoe@example.com", "", LocalDate.now(),
            "https://randomuser.me/api/portraits/men/77.jpg"),
        new User("Jane Doe", "jane@example.com", "", LocalDate.now(),
            "https://randomuser.me/api/portraits/women/43.jpg"))) {
      
      @Override
      protected Component createTagPopupContent(User relatedItem) {
        return createTagPopupButton(relatedItem);
      }

    };
    tta.setWidthFull();
    return tta;
  }  

  private TaggableTextArea<User> createGridTaggableTextArea() {
    TaggableTextArea<User> tta = new TaggableTextArea<User>(getPopupItems()) {
      
      @Override
      protected Component createTagPopupContent(User relatedItem) {
        return createTagPopupButton(relatedItem);
      }

      @Override
      protected AbstractField<?, User> createSelector() {
        return TaggableTextAreaDemoView.createGridSelector(this.items,this);
      }
      
    };
    tta.setWidthFull();
    tta.addFocusListener(ev->Notification.show("Focus gained: " + ev.getSource().getValue()));
    tta.addBlurListener(ev->Notification.show("Focus lost: " + ev.getSource().getValue()));
    return tta;
  }
  
  private Button createTagPopupButton(User relatedItem) {
    return new Button("Show user information", ev -> {
      Notification
          .show("User: " + relatedItem.getName() + ", Email: " + relatedItem.getEmail());
    });
  }
  
  private void showUsedTags(List<User> users, TaggableTextArea<User> tta) {
    Notification.show("Users: " + users.stream()
        .map(item -> item.getName() + ", " + item.getEmail()).collect(Collectors.joining(",")));
    Notification
        .show("Popup is " + (tta.isPopupOpened()?"":"not") + " opened.");
  }
  
  private static final String[] RANDOM_USERS_PICTURES =
      new String[] {"https://randomuser.me/api/portraits/men/35.jpg",
          "https://randomuser.me/api/portraits/women/51.jpg",
          "https://randomuser.me/api/portraits/men/77.jpg",
          "https://randomuser.me/api/portraits/women/43.jpg"};

  private List<User> getPopupItems() {
    List<User> items = new ArrayList<User>();
    for (int i = 0; i < 10000; i++) {
      String name = "user" + (i + 1);
      items.add(new User(name, name + "@example.com", "", LocalDate.now(),
          RANDOM_USERS_PICTURES[(int) (Math.random() * 4)]));
    }
    return items;
  }

  protected static AbstractField<?, User> createGridSelector(List<User> items, TaggableTextArea<User> taggableTextArea) {
    FilterListSelector<User> selector = new FilterListSelector<>(items, createItemRenderer(taggableTextArea));
    selector.setWidth("200px");
    selector.setFilterExpression(
        (item, filter) -> item.getName().toLowerCase().contains(filter.toLowerCase()));
    return selector;
  }

  private static ComponentRenderer<Component, User> createItemRenderer(TaggableTextArea<User> taggableTextArea) {
    return new ComponentRenderer<Component, User>(user -> {
      HorizontalLayout row = new HorizontalLayout();
      row.setAlignItems(FlexComponent.Alignment.CENTER);

      Avatar avatar = new Avatar();
      avatar.setName(user.getName());
      avatar.setImage(user.getPictureUrl());

      Span name = new Span(user.getName());
      name.addClickListener(ev->{
         Notification
            .show("Popup is " + (taggableTextArea.isPopupOpened()?"":"not") + " opened.");
      });
      Span profession = new Span(user.getEmail());
      profession.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size",
          "var(--lumo-font-size-s)");

      VerticalLayout column = new VerticalLayout(name, profession);
      column.setPadding(false);
      column.setSpacing(false);

      row.add(avatar, column);
      row.getStyle().set("line-height", "var(--lumo-line-height-m)");
      return row;
    });
  }

  // end-source-example
}
