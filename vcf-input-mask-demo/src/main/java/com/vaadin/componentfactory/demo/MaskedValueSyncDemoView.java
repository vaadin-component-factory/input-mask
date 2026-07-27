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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import java.util.List;

/**
 * Demonstrates that the value delivered to the server (via
 * {@link TextField#getValue()} and the value-change event) matches the masked
 * value, even with a live {@link ValueChangeMode} such as {@code TIMEOUT}.
 *
 * <p>Before the fix, typing an extra character into an already-full field made
 * the server receive the raw, pre-mask text (for example {@code 12345678901}
 * instead of {@code (123) 456-7890}), because the host field captured the value
 * on {@code input} before IMask reformatted it.
 *
 * <p>The Grid pushes a value into the editor from the server, exercising the
 * programmatic {@code setValue} path that must not be disturbed by the sync.
 */
@SuppressWarnings("serial")
@Route(value = "masked-value-sync", layout = MainLayout.class)
public class MaskedValueSyncDemoView extends BaseDemoView {

  public MaskedValueSyncDemoView() {
    addClassName("demo-view");

    TextField phoneField = new TextField("Phone (TIMEOUT mode)");
    phoneField.setPlaceholder(PHONE_MASK);
    phoneField.setValueChangeMode(ValueChangeMode.TIMEOUT);
    phoneField.setValueChangeTimeout(50);
    InputMask mask = new InputMask(PHONE_MASK);
    mask.extend(phoneField);

    Div committed = createMessageDiv("masked-value-sync-committed");
    phoneField.addValueChangeListener(ev ->
        committed.setText("getValue()=\"" + ev.getValue() + "\""));
    phoneField.setId("masked-value-sync-field");

    TextField eagerField = new TextField("Phone (EAGER mode)");
    eagerField.setPlaceholder(PHONE_MASK);
    eagerField.setValueChangeMode(ValueChangeMode.EAGER);
    InputMask eagerMask = new InputMask(PHONE_MASK);
    eagerMask.extend(eagerField);
    Div eagerCommitted = createMessageDiv("masked-value-sync-eager-committed");
    eagerField.addValueChangeListener(ev ->
        eagerCommitted.setText("getValue()=\"" + ev.getValue() + "\""));
    eagerField.setId("masked-value-sync-eager-field");

    Grid<Person> grid = new Grid<>(Person.class, false);
    grid.addColumn(Person::getFirstName).setHeader("Name");
    grid.addColumn(Person::getPhone).setHeader("Phone (raw)");
    grid.setItems(List.of(
        new Person("Alice", "A", "1234567890"),
        new Person("Bob", "B", "5559876543")));
    grid.setSelectionMode(SelectionMode.SINGLE);
    grid.setAllRowsVisible(true);
    grid.setId("masked-value-sync-grid");
    grid.addSelectionListener(ev -> ev.getFirstSelectedItem()
        .ifPresent(p -> phoneField.setValue(p.getPhone())));

    Paragraph typingInfo = new Paragraph("Type digits into either field, including one past "
        + "a full number - the committed value below each field stays masked (e.g. typing "
        + "\"12345678901\" commits \"(123) 456-7890\", not \"12345678901\").");

    Paragraph gridInfo = new Paragraph("Selecting a row below calls setValue(...) on the "
        + "TIMEOUT field from the server with the raw phone number. The field displays it "
        + "masked; getValue() returns exactly what was set (the raw value), and the "
        + "programmatic update is never reverted by the user-input sync.");

    add(createCard("Live typing keeps getValue() masked",
        typingInfo, phoneField, committed, eagerField, eagerCommitted));
    add(createCard("Server-side setValue() from a Grid fills the TIMEOUT field",
        gridInfo, grid));
  }
}
