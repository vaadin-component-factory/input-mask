package com.vaadin.componentfactory.demo;

import com.vaadin.componentfactory.addons.inputmask.InputMask;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

@SuppressWarnings("serial")
@Route(value = "binder-unmasked", layout = MainLayout.class)
public class BinderWithUnmaskedValueDemoView extends BaseDemoView {

  public BinderWithUnmaskedValueDemoView() {
    addClassName("demo-view");
    createInputMaskOnTextFieldWithBinderDemo();
  }

  private void createInputMaskOnTextFieldWithBinderDemo() {
    Div message = createMessageDiv("date-picker-with-binder-demo-message");

    // create text field
    TextField phoneField = new TextField("Phone");
    phoneField.setPlaceholder(PHONE_MASK);
    phoneField
        .setHelperText("Binder validation will be triggered if entered phone number is 123456.");
    phoneField.setValueChangeMode(ValueChangeMode.ON_BLUR);

    // create input mask for text field
    InputMask phoneFieldMask = new InputMask(PHONE_MASK);
    phoneFieldMask.extend(phoneField);

    // define binder
    Binder<Person> binder = new Binder<>();

    // to bind the field to the unmasked value, use the input mask field
    binder.forField(phoneFieldMask).withValidator(value -> {
      message.setText("Value on binder: " + value);
      return !"123456".equals(value);
    }, "Has to be different from 123456").bind(Person::getPhone, Person::setPhone);

    // set bean to binder
    Person person = new Person();
    person.setPhone("1112223333");
    binder.setBean(person);

    Paragraph description = new Paragraph(
        "Linking binder with InputMask's unsmaked value is a special use case only supported for TextField. "
            + "IllegalArgumentException will be thrown if a different type of field is used. "
            + "In order to allow binder to use the unmasked value from the InputMask, binder should be defined using the InputMask field instead of the actual text field.");

    HorizontalLayout buttonsLayout =
        new HorizontalLayout(new Button("Set default phone number", e -> phoneFieldMask.setValue("4445556666")),
            new Button("Clear phone number", e -> phoneFieldMask.clear()));
    buttonsLayout.setClassName("buttons-layout");
    
    add(createCard("Text field with input mask unmasked value binding", description, phoneField,
        message, buttonsLayout));
  }

}
