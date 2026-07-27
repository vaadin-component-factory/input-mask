package com.vaadin.componentfactory.demo;

import static com.vaadin.componentfactory.addons.inputmask.InputMaskOption.option;
import com.vaadin.componentfactory.addons.inputmask.InputMask;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.router.Route;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("serial")
@Route(value = "binder-masked", layout = MainLayout.class)
public class BinderDemoView extends BaseDemoView {

  public BinderDemoView() {
    addClassName("demo-view");
    createInputMaskOnTextFieldWithBinderDemo();
    createInputMaskOnDatePickerWithBinderDemo();
    createInputMaskOnTextFielOnGridCellDemo();
  }

  private void createInputMaskOnTextFieldWithBinderDemo() {
    Div message = createMessageDiv("text-field-with-binder-demo-message");

    // create text field
    TextField phoneField = new TextField("Phone");
    phoneField.setPlaceholder(PHONE_MASK);
    phoneField.setHelperText(
        "Binder validation will be triggered if entered phone number has length < 14.");

    // create input mask for text field
    InputMask phoneFieldMask = new InputMask(PHONE_MASK);
    phoneFieldMask.extend(phoneField);

    // define binder
    Binder<Person> binder = new Binder<Person>();

    // bind phone field
    binder.forField(phoneField).withNullRepresentation("").asRequired("Field is required")
        .withValidator(this::validatePhone).bind(Person::getPhone, Person::setPhone);

    // set bean to binder
    Person person = new Person();
    person.setPhone("(111) 222-3333");
    binder.setBean(person);

    // add value change listener to text field
    phoneField.addValueChangeListener(ev -> {
      phoneFieldMask.getUnmaskedValue(unmasked -> {
        message.setText(
            "Component value: " + phoneField.getValue() + " - Unmasked value: " + unmasked);
      });
    });

    add(createCard("Simple input mask on text field with binder", phoneField, message));
  }

  private void createInputMaskOnDatePickerWithBinderDemo() {
    Div message = createMessageDiv("date-picker-with-binder-demo-message");

    // create date picker
    DatePicker dateField = new DatePicker("Date");
    dateField.setI18n(new DatePickerI18n().setDateFormat(DATE_FORMAT));
    dateField.setPlaceholder(DATE_FORMAT);
    dateField.setHelperText("Binder validation will be triggered if date is 01/01/2001.");

    // create input mask for date picker
    InputMask dateFieldMask = new InputMask(DATE_MASK, option("overwrite", true));
    dateFieldMask.extend(dateField);

    // define binder
    Binder<Person> binder = new Binder<>();

    // bind date field
    binder.forField(dateField).withValidator(this::validateBirthDay).bind(Person::getBirthday, Person::setBirthday);

    // set read bean to binder
    Person person = new Person();
    person.setBirthday(LocalDate.of(2021, 12, 23));
    binder.readBean(person);

    // add value change listener to date field
    dateField.addValueChangeListener(ev -> {
      message.setText("Component value: " + dateField.getValue());
    });

    HorizontalLayout buttonsLayout =
        new HorizontalLayout(new Button("Set today", e -> dateField.setValue(LocalDate.now())),
            new Button("Clear date", e -> dateField.clear()));
    buttonsLayout.setClassName("buttons-layout");

    add(createCard("Simple input mask on date picker with binder", dateField, message,
        buttonsLayout));
  }

  protected ValidationResult validateBirthDay(LocalDate value, ValueContext ctx) {
    if (value != null && value.equals(LocalDate.of(2001, 01, 01))) {
      return ValidationResult.error("Has to be different from 01/01/2001");
    }
    return ValidationResult.ok();
  }

  private void createInputMaskOnTextFielOnGridCellDemo() {
    ValidationMessage phoneValidationMessage = new ValidationMessage();

    Grid<Person> grid = new Grid<>(Person.class, false);
    grid.setHeight("200px");
    Grid.Column<Person> firstNameColumn = grid.addColumn(Person::getFirstName)
        .setHeader("First name").setWidth("150px").setFlexGrow(0);
    Grid.Column<Person> lastNameColumn =
        grid.addColumn(Person::getLastName).setHeader("Last name").setWidth("150px").setFlexGrow(0);
    Grid.Column<Person> phoneColumn = grid.addColumn(Person::getPhone).setHeader("Phone");

    Binder<Person> binder = new Binder<>(Person.class);
    Editor<Person> editor = grid.getEditor();
    editor.setBinder(binder);

    TextField phoneField = new TextField();
    new InputMask(PHONE_MASK).extend(phoneField);
    phoneField.setWidthFull();
    addCloseHandler(phoneField, editor);
    binder.forField(phoneField).asRequired("Phone must not be empty")
        .withValidator(this::validatePhone).withStatusLabel(phoneValidationMessage)
        .bind(Person::getPhone, Person::setPhone);
    phoneColumn.setEditorComponent(phoneField);

    grid.addItemDoubleClickListener(e -> {
      editor.editItem(e.getItem());
      Component editorComponent = e.getColumn().getEditorComponent();
      if (editorComponent instanceof Focusable) {
        ((Focusable) editorComponent).focus();
      }
    });

    editor.addCancelListener(e -> {
      phoneValidationMessage.setText("");
    });

    List<Person> people = this.getPeople();
    grid.setItems(people);

    grid.setId("input-mask-on-text-field-on-grid-cell");

    add(createCard("Input mask on text field on grid cell", grid, phoneValidationMessage));
  }

  private List<Person> getPeople() {
    List<Person> people = Arrays.asList(new Person("Alex", "Jones", "(333) 445-5859"),
        new Person("John", "Jackson", "(333) 898-9999"),
        new Person("Tom", "Ellison", "(333) 455-8978"));
    return people;
  }
}
