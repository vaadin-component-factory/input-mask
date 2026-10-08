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
import com.vaadin.componentfactory.addons.inputmask.InputMaskOption;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * Demo for a non-lazy mask whose definitions also accept a space, the same
 * character used as the placeholder. This replicates a legacy masked field
 * where blanks are valid input, and the template (e.g.
 * {@code "  -    -   -   -  -      "}) is always visible.
 *
 * <p>The blank template is ambiguous with these definitions: read back from
 * the input it is a complete value of typed spaces. The cards cover the
 * situations where a new IMask instance starts on an input that still shows
 * the template, or on an input that already has focus. In both cases the
 * field must stay empty and typing must fill the slots from the start.
 *
 * @author Vaadin Ltd
 */
@SuppressWarnings("serial")
@Route(value = "space-definitions-mask", layout = MainLayout.class)
public class SpaceDefinitionsMaskDemoView extends BaseDemoView {

  static final String MASK = "00-aaa*-aaa-aaa-00-000000";
  static final String SHORT_MASK = "00-aaa*-aaa-00-000000";

  private static final InputMaskOption DEFINITIONS_WITH_SPACE = option(
      "definitions",
      "{'0': /[\\d ]/, 'a': /[A-Za-z ]/, 'A': /[A-Z ]/, '*': /[A-Za-z0-9 ]/}",
      true);

  public SpaceDefinitionsMaskDemoView() {
    addClassName("demo-view");
    createRecreateMaskDemo();
    createSwitchMaskDemo();
    createFocusThenMaskDemo();
  }

  /**
   * remove() followed by a new InputMask on the same field: the new IMask
   * starts on an input that still shows the previous instance's template.
   */
  private void createRecreateMaskDemo() {
    TextField field = new TextField("Legacy code");
    field.setId("space-definitions-recreate-text-field");
    field.setWidth("400px");

    InputMask[] maskHolder = { createMask(MASK) };
    maskHolder[0].extend(field);

    Button recreate = new Button("Re-create mask", e -> {
      maskHolder[0].remove();
      field.clear();
      maskHolder[0] = createMask(MASK);
      maskHolder[0].extend(field);
    });
    recreate.setId("space-definitions-recreate-button");

    add(createCard("Re-create the mask on an empty field",
        new Paragraph("Removes the InputMask and extends the field with a new one."),
        field, recreate));
  }

  /**
   * setMask() on an empty, masked field: the wrapper re-creates IMask on an
   * input that still shows the previous mask's template.
   */
  private void createSwitchMaskDemo() {
    TextField field = new TextField("Legacy code");
    field.setId("space-definitions-switch-text-field");
    field.setWidth("400px");

    InputMask inputMask = createMask(MASK);
    inputMask.extend(field);

    Button switchMask = new Button("Switch mask", e -> {
      inputMask.setMask(SHORT_MASK);
      field.clear();
    });
    switchMask.setId("space-definitions-switch-button");

    add(createCard("Switch the mask of an empty field",
        new Paragraph("Calls setMask(\"" + SHORT_MASK + "\") on the existing InputMask."),
        field, switchMask));
  }

  /**
   * The field is focused first and the mask is applied in the next round
   * trip, as when an application sets the mask after loading data.
   */
  private void createFocusThenMaskDemo() {
    TextField field = new TextField("Legacy code");
    field.setId("space-definitions-focus-text-field");
    field.setWidth("400px");

    Button focusThenMask = new Button("Focus, then set mask",
        e -> field.getElement().executeJs("this.focus()")
            .then(ignore -> createMask(MASK).extend(field)));
    focusThenMask.setId("space-definitions-focus-button");

    add(createCard("Apply the mask to a focused field",
        new Paragraph("Focuses the unmasked field, then extends it with the mask."),
        field, focusThenMask));
  }

  private static InputMask createMask(String mask) {
    InputMask inputMask = new InputMask(mask, lazy(false),
        option("placeholderChar", " "), DEFINITIONS_WITH_SPACE);
    inputMask.setAllowWhitespace(true);
    return inputMask;
  }
}
