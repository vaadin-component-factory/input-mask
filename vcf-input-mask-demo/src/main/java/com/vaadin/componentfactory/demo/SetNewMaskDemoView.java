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

import com.vaadin.componentfactory.addons.inputmask.InputMask;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * Demo view for the {@code setMask} feature on {@link InputMask}.
 * <p>
 * The view exposes a {@link Select} that switches the active mask of a single
 * {@link TextField} between three patterns. Each selection calls
 * {@link InputMask#setMask(String)} on the same wrapper instance, so the
 * field is updated in place without re-creating the wrapper.
 *
 * @author Vaadin Ltd
 */
@SuppressWarnings("serial")
@Route(value = "set-new-mask", layout = MainLayout.class)
public class SetNewMaskDemoView extends BaseDemoView {

  private static final String PHONE_US_MASK = "(000) 000-0000";
  private static final String PHONE_INTL_MASK = "+00 000 000 0000";
  private static final String REFERENCE_MASK = "a-00000000-a";

  public SetNewMaskDemoView() {
    addClassName("demo-view");
    createSetMaskOnTextFieldDemo();
  }

  private void createSetMaskOnTextFieldDemo() {
    Div message = createMessageDiv("set-new-mask-demo-message");
    Span maskedValueSpan = new Span();
    Span unmaskedValueSpan = new Span();

    TextField field = new TextField("Value");
    field.setPlaceholder(PHONE_US_MASK);

    InputMask mask = new InputMask(PHONE_US_MASK);
    mask.extend(field);

    Select<String> maskSelect = new Select<>();
    maskSelect.setLabel("Mask");
    maskSelect.setItems(PHONE_US_MASK, PHONE_INTL_MASK, REFERENCE_MASK);
    maskSelect.setValue(PHONE_US_MASK);
    maskSelect.addValueChangeListener(ev -> {
      String newMask = ev.getValue();
      if (newMask != null) {
        mask.setMask(newMask);
        field.setPlaceholder(newMask);
        field.clear();
      }
    });

    field.addValueChangeListener(ev -> {
      mask.getMaskedValue(masked -> maskedValueSpan
          .setText("Masked value: \"" + masked + "\""));
      mask.getUnmaskedValue(unmasked -> unmaskedValueSpan
          .setText(" - Unmasked value: \"" + unmasked + "\""));
      message.add(maskedValueSpan, unmaskedValueSpan);
    });

    field.setId("set-new-mask-text-field");
    maskSelect.setId("set-new-mask-select");

    Paragraph description = new Paragraph(
        "Pick a different mask in the dropdown to switch the active mask of the "
            + "text field at runtime. The wrapper instance is reused; only the mask "
            + "options are updated. The field is cleared on each change so the new "
            + "mask can be tried with a fresh value.");

    add(createCard("Switch mask at runtime", description, maskSelect, field, message));
  }
}
