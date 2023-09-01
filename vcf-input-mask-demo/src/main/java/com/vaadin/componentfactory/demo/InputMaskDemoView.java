/*
 * Copyright 2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.componentfactory.demo;

import static com.vaadin.componentfactory.addons.inputmask.InputMaskOption.option;

import com.vaadin.componentfactory.addons.inputmask.InputMask;
import com.vaadin.componentfactory.addons.inputmask.InputMaskOption;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import java.util.Arrays;
import java.util.List;

/**
 * View for {@link InputMask} demo.
 *
 * @author Vaadin Ltd
 */
@Route("")
@CssImport("./styles/demo.css")
public class InputMaskDemoView extends VerticalLayout {

	public InputMaskDemoView() {		
		addClassName("demo-view");		
		createBasicInputMaskOnTextFieldDemo();
		createBasicInputMaskOnDatePickerDemo();
		createInputMaskOnTextFieldWithBinderDemo();
		createInputMaskOnTextFielOnGridCellDemo();
		createNumberInputMaskOnTextFieldDemo();
		createRegExpInputMaskOnTextFieldDemo();
	}

	private void createNumberInputMaskOnTextFieldDemo() {
		Div message = createMessageDiv("number-input-mask-on-text-field-demo-message");
		Span maskedValueSpan = new Span();
		Span unmaskedValueSpan = new Span();

		TextField numberField = new TextField("Number");
		InputMask inputMask = new InputMask("Number", true,
				InputMaskOption.option("scale", 2),
				InputMaskOption.option("thousandsSeparator", "-"),
				InputMaskOption.option("radix", '.')
		);

		inputMask
				.extend(numberField);
		numberField.setValueChangeMode(ValueChangeMode.ON_BLUR);

		numberField.addValueChangeListener(ev -> {
			inputMask.getMaskedValue(masked -> {
				maskedValueSpan.setText("Masked value: " + masked);
			});
			inputMask.getUnmaskedValue(unmasked -> {
				unmaskedValueSpan.setText(" - Unmasked value: " + unmasked);
			});
			message.add(maskedValueSpan, unmaskedValueSpan);
		});

		numberField.setId("number-input-mask-on-text-field");

		add(createCard("Simple number mask on text field", numberField, message));
	}

	private void createRegExpInputMaskOnTextFieldDemo() {
		Div message = createMessageDiv("regexp-input-mask-on-text-field-demo-message");
		Span maskedValueSpan = new Span();
		Span unmaskedValueSpan = new Span();

		TextField numberField = new TextField("Regexp digits");
		InputMask inputMask = new InputMask("/^\\d+$/", true);

		inputMask.extend(numberField);

		numberField.addValueChangeListener(ev -> {
			inputMask.getMaskedValue(masked -> {
				maskedValueSpan.setText("Masked value: " + masked);
			});
			inputMask.getUnmaskedValue(unmasked -> {
				unmaskedValueSpan.setText(" - Unmasked value: " + unmasked);
			});
			message.add(maskedValueSpan, unmaskedValueSpan);
		});

		numberField.setId("regexp-input-mask-on-text-field");

		add(createCard("Simple regexp input mask on text field", numberField, message));
	}

	private void createBasicInputMaskOnTextFieldDemo() {
		Div message = createMessageDiv("simple-input-mask-on-text-field-demo-message");
		Span maskedValueSpan = new Span();
		Span unmaskedValueSpan = new Span();

		TextField phoneField = new TextField("Phone");
		phoneField.setPlaceholder(PHONE_MASK);
		InputMask phoneFieldMask = new InputMask(PHONE_MASK);
		phoneFieldMask.extend(phoneField);

		phoneField.addValueChangeListener(ev -> {
			phoneFieldMask.getMaskedValue(masked -> {
				maskedValueSpan.setText("Masked value: " + masked);
			});
			phoneFieldMask.getUnmaskedValue(unmasked -> {
				unmaskedValueSpan.setText(" - Unmasked value: " + unmasked);
			});
			message.add(maskedValueSpan, unmaskedValueSpan);
		});

		phoneField.setId("simple-input-mask-on-text-field");

		add(createCard("Simple input mask on text field", phoneField, message));
	}

	private void createBasicInputMaskOnDatePickerDemo() {
		Div message = createMessageDiv("simple-input-mask-on-date-picker-demo-message");

		DatePicker dateField = new DatePicker("Date");
		dateField.setI18n(new DatePickerI18n().setDateFormat(DATE_FORMAT));
		dateField.setPlaceholder(DATE_FORMAT);
		new InputMask(DATE_MASK, option("overwrite", true)).extend(dateField);

		dateField.addValueChangeListener(ev -> {
			message.setText("Component value: " + dateField.getValue());
		});
		
		dateField.setId("simple-input-mask-on-date-picker");

		add(createCard("Simple input mask on date picker", dateField, message));
	}

	private void createInputMaskOnTextFieldWithBinderDemo() {
		Div message = createMessageDiv("simple-input-mask-on-text-field-with-binder-demo-message");

		TextField phoneField = new TextField("Phone");
		phoneField.setPlaceholder(PHONE_MASK);
		phoneField.setHelperText("Binder validation will be triggered if entered phone number has length < 14.");
		InputMask phoneFieldMask = new InputMask(PHONE_MASK);
		phoneFieldMask.extend(phoneField);

		Binder<Person> binder = new Binder<Person>();
		binder.setBean(new Person());

		binder.forField(phoneField).withNullRepresentation("").asRequired("Field is required")
				.withValidator(this::validatePhone).bind(Person::getPhone, Person::setPhone);

		phoneField.addValueChangeListener(ev -> {
			message.setText("Component value: " + phoneField.getValue());
		});

		phoneField.setId("simple-input-mask-on-text-field-with-binder");

		add(createCard("Simple input mask on text field with binder", phoneField, message));
	}

	private void createInputMaskOnTextFielOnGridCellDemo() {
		ValidationMessage phoneValidationMessage = new ValidationMessage();

		Grid<Person> grid = new Grid<>(Person.class, false);
		grid.setHeight("200px");
		Grid.Column<Person> firstNameColumn = grid.addColumn(Person::getFirstName).setHeader("First name")
				.setWidth("150px").setFlexGrow(0);
		Grid.Column<Person> lastNameColumn = grid.addColumn(Person::getLastName).setHeader("Last name")
				.setWidth("150px").setFlexGrow(0);
		Grid.Column<Person> phoneColumn = grid.addColumn(Person::getPhone).setHeader("Phone");

		Binder<Person> binder = new Binder<>(Person.class);
		Editor<Person> editor = grid.getEditor();
		editor.setBinder(binder);

		TextField phoneField = new TextField();
		new InputMask(PHONE_MASK).extend(phoneField);
		phoneField.setWidthFull();
		addCloseHandler(phoneField, editor);
		binder.forField(phoneField).asRequired("Phone must not be empty").withValidator(this::validatePhone)
				.withStatusLabel(phoneValidationMessage).bind(Person::getPhone, Person::setPhone);
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
				new Person("John", "Jackson", "(333) 898-9999"), new Person("Tom", "Ellison", "(333) 455-8978)"));
		return people;
	}

	/**
	 * Additional code used in the demo
	 */
	private static final String PHONE_MASK = "(000) 000-0000";
	private static final String DATE_MASK = "00/00/0000";
	private static final String DATE_FORMAT = "MM/dd/yyyy";

	private static void addCloseHandler(Component textField, Editor<Person> editor) {
		textField.getElement().addEventListener("keydown", e -> editor.cancel()).setFilter("event.code === 'Escape'");
	}

	private ValidationResult validatePhone(String phone, ValueContext ctx) {
		if (phone != null && phone.length() < 14) {
			return ValidationResult.error("Enter a valid phone number. Length cannot be less than 14");
		}
		return ValidationResult.ok();
	}

	private Div createMessageDiv(String id) {
		Div message = new Div();
		message.setId(id);
		message.getStyle().set("whiteSpace", "pre");
		return message;
	}
	
	private Div createCard(String title, Component... components) {		
		Div card = new Div();
		card.setWidthFull();
		card.addClassName("component-card");
		card.add(new H3(title));
		card.add(components);
		return card;
	}
}
