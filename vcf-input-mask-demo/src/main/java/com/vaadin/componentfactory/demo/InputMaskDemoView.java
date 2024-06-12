/*
 * Copyright 2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.componentfactory.demo;

import static com.vaadin.componentfactory.addons.inputmask.InputMaskOption.option;
import com.vaadin.componentfactory.addons.inputmask.InputMask;
import com.vaadin.componentfactory.addons.inputmask.InputMaskOption;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

/**
 * {@link InputMask} basic use demo view.
 *
 * @author Vaadin Ltd
 */
@SuppressWarnings("serial")
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "basic-use", layout = MainLayout.class)
public class InputMaskDemoView extends BaseDemoView {

  public InputMaskDemoView() {
    addClassName("demo-view");
    createBasicInputMaskOnTextFieldDemo();
    createBasicInputMaskOnDatePickerDemo();
    createNumberInputMaskOnTextFieldDemo();
    createRegExpInputMaskOnTextFieldDemo();
    createBasicInputMaskOnTextAreaDemo();
  }

  private void createNumberInputMaskOnTextFieldDemo() {
    Div message = createMessageDiv("number-input-mask-on-text-field-demo-message");
    Span maskedValueSpan = new Span();
    Span unmaskedValueSpan = new Span();

    TextField numberField = new TextField("Number");
    InputMask inputMask = new InputMask("Number", true, InputMaskOption.option("scale", 2),
        InputMaskOption.option("thousandsSeparator", "-"), InputMaskOption.option("radix", '.'));

    inputMask.extend(numberField);
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
    InputMask dateFieldMask = new InputMask(DATE_MASK, option("overwrite", true));
    dateFieldMask.extend(dateField);

    dateField.addValueChangeListener(ev -> {
      message.setText("Component value: " + dateField.getValue());
    });

    dateField.setId("simple-input-mask-on-date-picker");

    add(createCard("Simple input mask on date picker", dateField, message));
  }

  private void createBasicInputMaskOnTextAreaDemo() {
    Div message = createMessageDiv("simple-input-mask-on-text-area-demo-message");
    Span maskedValueSpan = new Span();
    Span unmaskedValueSpan = new Span();


    TextArea phoneField = new TextArea("Phone");
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

    phoneField.setId("simple-input-mask-on-text-area");

    add(createCard("Simple input mask on text area", phoneField, message));
  }
}
