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
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * Demo view for the {@code setMask} feature on {@link InputMask}.
 * <p>
 * The first card exposes a {@link Select} that switches the active mask of a
 * single {@link TextField} between three patterns. Each selection calls
 * {@link InputMask#setMask(String)} on the same wrapper instance, so the
 * field is updated in place without re-creating the wrapper.
 * <p>
 * The second card combines {@code setMask} with
 * {@link InputMask#setAllowWhitespace(boolean)} to demonstrate that the
 * whitespace toggle persists across mask changes.
 *
 * @author Vaadin Ltd
 */
@SuppressWarnings("serial")
@Route(value = "set-new-mask", layout = MainLayout.class)
public class SetNewMaskDemoView extends BaseDemoView {

  private static final String PHONE_US_MASK = "(000) 000-0000";
  private static final String PHONE_INTL_MASK = "+00 000 000 0000";
  private static final String REFERENCE_MASK = "a-00000000-a";

  private static final String REGEX_ANY_MASK = "/^.*$/";
  private static final String WILDCARD_PATTERN_MASK = "*-00000000-a";

  public SetNewMaskDemoView() {
    addClassName("demo-view");
    createSetMaskOnTextFieldDemo();
    createSetMaskWithWhitespaceToggleDemo();
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

  private void createSetMaskWithWhitespaceToggleDemo() {
    Div message = createMessageDiv("set-new-mask-whitespace-demo-message");
    Span maskedValueSpan = new Span();

    TextField field = new TextField("Value");

    // Start with the regex mask (must be eval'd on the client) so the
    // whitespace toggle has a visible effect on the very first interaction.
    InputMask mask = new InputMask(REGEX_ANY_MASK, true);
    mask.extend(field);

    Select<String> maskSelect = new Select<>();
    maskSelect.setLabel("Mask");
    maskSelect.setItems(REGEX_ANY_MASK, WILDCARD_PATTERN_MASK);
    maskSelect.setValue(REGEX_ANY_MASK);
    maskSelect.addValueChangeListener(ev -> {
      String newMask = ev.getValue();
      if (newMask == null) {
        return;
      }
      // The regex variant must be evaluated on the client; the pattern
      // variant is a plain string.
      mask.setMask(newMask, REGEX_ANY_MASK.equals(newMask));
      field.clear();
    });

    Checkbox whitespaceToggle = new Checkbox("Allow whitespace");
    whitespaceToggle.addValueChangeListener(ev -> mask.setAllowWhitespace(ev.getValue()));

    field.addValueChangeListener(ev -> {
      mask.getMaskedValue(masked -> maskedValueSpan
          .setText("Masked value: \"" + masked + "\""));
      message.add(maskedValueSpan);
    });

    field.setId("set-new-mask-whitespace-text-field");
    maskSelect.setId("set-new-mask-whitespace-select");
    whitespaceToggle.setId("set-new-mask-whitespace-toggle");

    Paragraph description = new Paragraph(
        "Combines setMask with setAllowWhitespace. Toggle the checkbox to allow "
            + "or block leading whitespace, then switch the mask via the "
            + "dropdown. The whitespace setting persists across mask changes, "
            + "so the two features can be used together without re-creating "
            + "the wrapper.");

    add(createCard("Switch mask + whitespace toggle", description, maskSelect,
        whitespaceToggle, field, message));
  }
}
