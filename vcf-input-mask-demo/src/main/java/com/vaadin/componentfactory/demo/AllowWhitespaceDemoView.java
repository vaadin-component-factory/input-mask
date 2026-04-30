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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * Demo view for the {@code allowWhitespace} feature on {@link InputMask}.
 * <p>
 * The first card uses a regex mask that accepts any character (including
 * spaces) and exposes a checkbox to toggle the feature on and off at runtime,
 * so the difference can be observed without reloading the view.
 * <p>
 * The second card uses a {@link TextArea} together with the same toggle to
 * show that the feature works for multi-line components as well.
 *
 * @author Vaadin Ltd
 */
@SuppressWarnings("serial")
@Route(value = "allow-whitespace", layout = MainLayout.class)
public class AllowWhitespaceDemoView extends BaseDemoView {

  public AllowWhitespaceDemoView() {
    addClassName("demo-view");
    createAllowWhitespaceOnTextFieldDemo();
    createAllowWhitespaceOnTextAreaDemo();
    createAllowWhitespaceOnPatternMaskDemo();
  }

  private void createAllowWhitespaceOnTextFieldDemo() {
    Div message = createMessageDiv("allow-whitespace-on-text-field-demo-message");
    Span maskedValueSpan = new Span();
    Span unmaskedValueSpan = new Span();

    TextField sentenceField = new TextField("Sentence");
    sentenceField.setPlaceholder("Type any text including spaces");

    InputMask sentenceMask = new InputMask("/^.*$/", true);
    sentenceMask.extend(sentenceField);

    Checkbox toggle = new Checkbox("Allow whitespace");
    toggle.addValueChangeListener(e -> sentenceMask.setAllowWhitespace(e.getValue()));

    sentenceField.addValueChangeListener(ev -> {
      sentenceMask.getMaskedValue(masked -> {
        maskedValueSpan.setText("Masked value: \"" + masked + "\"");
      });
      sentenceMask.getUnmaskedValue(unmasked -> {
        unmaskedValueSpan.setText(" - Unmasked value: \"" + unmasked + "\"");
      });
      message.add(maskedValueSpan, unmaskedValueSpan);
    });

    sentenceField.setId("allow-whitespace-on-text-field");
    toggle.setId("allow-whitespace-on-text-field-toggle");

    Paragraph description = new Paragraph(
        "Toggle the checkbox to enable or disable whitespace input. With the toggle off "
            + "(default behaviour), pressing space at the start of the field is blocked and "
            + "selecting all + space clears the field. With the toggle on, spaces are passed "
            + "through and IMask decides whether to accept them based on the mask.");

    add(createCard("Allow whitespace on text field (regex mask)", description, toggle,
        sentenceField, message));
  }

  private void createAllowWhitespaceOnTextAreaDemo() {
    Div message = createMessageDiv("allow-whitespace-on-text-area-demo-message");
    Span maskedValueSpan = new Span();

    TextArea notesField = new TextArea("Notes");
    notesField.setPlaceholder("Type or paste any text including spaces");

    InputMask notesMask = new InputMask("/^[\\s\\S]*$/", true);
    notesMask.extend(notesField);
    notesMask.setAllowWhitespace(true);

    Checkbox toggle = new Checkbox("Allow whitespace", true);
    toggle.addValueChangeListener(e -> notesMask.setAllowWhitespace(e.getValue()));

    notesField.addValueChangeListener(ev -> {
      notesMask.getMaskedValue(masked -> {
        maskedValueSpan.setText("Masked value: \"" + masked + "\"");
      });
      message.add(maskedValueSpan);
    });

    notesField.setId("allow-whitespace-on-text-area");
    toggle.setId("allow-whitespace-on-text-area-toggle");

    add(createCard("Allow whitespace on text area (regex mask)", toggle, notesField, message));
  }

  private void createAllowWhitespaceOnPatternMaskDemo() {
    Div message = createMessageDiv("allow-whitespace-on-pattern-mask-demo-message");
    Span maskedValueSpan = new Span();
    Span unmaskedValueSpan = new Span();

    TextField patternField = new TextField("Reference code");
    patternField.setPlaceholder("*-00000000-a (first char may be a space)");

    InputMask patternMask = new InputMask("*-00000000-a");
    patternMask.extend(patternField);
    patternMask.setAllowWhitespace(true);

    Checkbox toggle = new Checkbox("Allow whitespace", true);
    toggle.addValueChangeListener(e -> patternMask.setAllowWhitespace(e.getValue()));

    patternField.addValueChangeListener(ev -> {
      patternMask.getMaskedValue(masked -> {
        maskedValueSpan.setText("Masked value: \"" + masked + "\"");
      });
      patternMask.getUnmaskedValue(unmasked -> {
        unmaskedValueSpan.setText(" - Unmasked value: \"" + unmasked + "\"");
      });
      message.add(maskedValueSpan, unmaskedValueSpan);
    });

    patternField.setId("allow-whitespace-on-pattern-mask");
    toggle.setId("allow-whitespace-on-pattern-mask-toggle");

    Paragraph description = new Paragraph(
        "Pattern mask \"*-00000000-a\" - the first character is the IMask wildcard \"*\" so it "
            + "accepts any character, including a whitespace, when the toggle is on. The next "
            + "block requires 8 digits and the last block requires a single letter.");

    add(createCard("Allow whitespace on pattern mask", description, toggle, patternField, message));
  }
}
