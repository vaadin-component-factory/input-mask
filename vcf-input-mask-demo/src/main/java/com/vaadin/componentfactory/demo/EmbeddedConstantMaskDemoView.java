/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import static com.vaadin.componentfactory.addons.inputmask.InputMaskOption.lazy;
import static com.vaadin.componentfactory.addons.inputmask.InputMaskOption.option;
import com.vaadin.componentfactory.addons.inputmask.InputMask;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

/**
 * Demo for masks that contain a constant block with an editable slot in front
 * of it, replicating a legacy masked text field whose mask was
 * {@code " 08000-  -  -      - "}: the digits {@code 08000} and the hyphens
 * are fixed, and the blanks are user input. Typing {@code 12345} into the
 * empty field produces {@code 108000-23-45}: the first character lands in the
 * slot before the constant block, and the caret then jumps over
 * {@code 08000-} to the next editable slot.
 *
 * <p>In the IMask pattern the constant digits are escaped with a backslash so
 * they are treated as fixed characters instead of digit placeholders.
 *
 * @author Vaadin Ltd
 */
@SuppressWarnings("serial")
@Route(value = "embedded-constant-mask", layout = MainLayout.class)
public class EmbeddedConstantMaskDemoView extends BaseDemoView {

  /**
   * One editable any-character slot ({@code *}), the constant block
   * {@code 08000}, then hyphen-separated editable groups. Escaped characters
   * ({@code \0}) are fixed literals; unescaped {@code 0}s are digit
   * placeholders and {@code *}s accept any character.
   */
  static final String EMBEDDED_CONSTANT_MASK = "*\\08\\0\\0\\0-00-**-000000-0";

  public EmbeddedConstantMaskDemoView() {
    addClassName("demo-view");
    createLegacyStyleMaskDemo();
    createLazyMaskDemo();
    createEagerValueChangeModeDemo();
  }

  /**
   * Same eager mask as the first card, but with {@link ValueChangeMode#EAGER}
   * and both a value change and a blur listener, mirroring applications whose
   * business logic requires the value change event to arrive before the blur
   * event. The message logs the server-side event sequence, which makes it
   * visible when an edit (e.g. a paste) does not produce a value change.
   */
  private void createEagerValueChangeModeDemo() {
    Div message = createMessageDiv("embedded-constant-mask-eager-vcm-demo-message");
    StringBuilder eventLog = new StringBuilder();

    TextField eagerField = new TextField("Legacy code (EAGER value change mode)");
    eagerField.setWidth("400px");
    eagerField.setValueChangeMode(ValueChangeMode.EAGER);
    InputMask inputMask = new InputMask(EMBEDDED_CONSTANT_MASK, lazy(false),
        option("placeholderChar", " "), option("eager", true));
    inputMask.extend(eagerField);

    eagerField.addValueChangeListener(ev -> {
      eventLog.append("[value-change: ").append(ev.getValue()).append(']');
      message.setText(eventLog.toString());
    });
    eagerField.addBlurListener(ev -> {
      eventLog.append("[blur]");
      message.setText(eventLog.toString());
    });

    eagerField.setId("embedded-constant-mask-eager-vcm-text-field");

    add(createCard("Eager mask with EAGER value change mode", eagerField, message));
  }

  private void createLegacyStyleMaskDemo() {
    Div message = createMessageDiv("embedded-constant-mask-demo-message");
    Span maskedValueSpan = new Span();
    Span unmaskedValueSpan = new Span();

    TextField legacyField = new TextField("Legacy code");
    legacyField.setWidth("400px");
    // lazy=false keeps the whole mask template visible, and the space
    // placeholder makes the empty slots render as blanks like the legacy
    // application did: " 08000-  -  -      - ". eager=true moves the caret
    // past the fixed characters as soon as a slot is filled, so after typing
    // the first digit the caret lands right after the constant "08000-".
    InputMask inputMask = new InputMask(EMBEDDED_CONSTANT_MASK, lazy(false),
        option("placeholderChar", " "), option("eager", true));
    inputMask.extend(legacyField);

    legacyField.addValueChangeListener(ev -> {
      inputMask.getMaskedValue(masked -> {
        maskedValueSpan.setText("Masked value: " + masked);
      });
      inputMask.getUnmaskedValue(unmasked -> {
        unmaskedValueSpan.setText(" - Unmasked value: " + unmasked);
      });
      message.add(maskedValueSpan, unmaskedValueSpan);
    });

    legacyField.setId("embedded-constant-mask-text-field");

    add(createCard("Editable slot before constant block 08000 (template always visible)",
        legacyField, message));
  }

  private void createLazyMaskDemo() {
    Div message = createMessageDiv("embedded-constant-mask-lazy-demo-message");
    Span maskedValueSpan = new Span();
    Span unmaskedValueSpan = new Span();

    TextField lazyField = new TextField("Legacy code (lazy)");
    InputMask inputMask = new InputMask(EMBEDDED_CONSTANT_MASK);
    inputMask.extend(lazyField);

    lazyField.addValueChangeListener(ev -> {
      inputMask.getMaskedValue(masked -> {
        maskedValueSpan.setText("Masked value: " + masked);
      });
      inputMask.getUnmaskedValue(unmasked -> {
        unmaskedValueSpan.setText(" - Unmasked value: " + unmasked);
      });
      message.add(maskedValueSpan, unmaskedValueSpan);
    });

    lazyField.setId("embedded-constant-mask-lazy-text-field");

    add(createCard("Editable slot before constant block 08000 (default lazy mask)",
        lazyField, message));
  }
}
