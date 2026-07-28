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

/**
 * Reproduces the scenario where a Grid row carries both a numeric value and a
 * mask, and selecting a row applies <em>both</em> {@link InputMask#setMask} and
 * {@code setValue} to the paired fields.
 *
 * <p>The interesting case is switching from a shorter mask to a longer one: e.g.
 * selecting Bob (mask {@code 00}, value {@code 20}) renders {@code 20}, then
 * selecting Alice (mask {@code 0000}, value {@code 2005}) should render
 * {@code 2005}. The bug under investigation is that the previous, shorter mask
 * is still active when the new value is applied, so {@code 2005} gets truncated
 * to {@code 20}.
 *
 * <p>The value is provided to both a TIMEOUT and an EAGER field so the two
 * value-change modes can be compared, mirroring {@link MaskedValueSyncDemoView}.
 * IMask digit masks are used ({@code 0} = one digit): {@code 00}, {@code 000},
 * {@code 0000}.
 */
@SuppressWarnings("serial")
@Route(value = "set-mask-with-value", layout = MainLayout.class)
public class SetMaskWithValueDemoView extends BaseDemoView {

  private record MaskSample(String name, String number, String mask) {
  }

  public SetMaskWithValueDemoView() {
    addClassName("demo-view");

    // TIMEOUT field + its mask
    TextField timeoutField = new TextField("Number (TIMEOUT mode)");
    timeoutField.setValueChangeMode(ValueChangeMode.TIMEOUT);
    timeoutField.setValueChangeTimeout(50);
    InputMask timeoutMask = new InputMask("0000");
    timeoutMask.extend(timeoutField);
    Div timeoutCommitted = createMessageDiv("set-mask-with-value-timeout-committed");
    timeoutField.addValueChangeListener(ev ->
        timeoutCommitted.setText("getValue()=\"" + ev.getValue() + "\""));
    timeoutField.setId("set-mask-with-value-timeout-field");

    // EAGER field + its mask
    TextField eagerField = new TextField("Number (EAGER mode)");
    eagerField.setValueChangeMode(ValueChangeMode.EAGER);
    InputMask eagerMask = new InputMask("0000");
    eagerMask.extend(eagerField);
    Div eagerCommitted = createMessageDiv("set-mask-with-value-eager-committed");
    eagerField.addValueChangeListener(ev ->
        eagerCommitted.setText("getValue()=\"" + ev.getValue() + "\""));
    eagerField.setId("set-mask-with-value-eager-field");

    Grid<MaskSample> grid = new Grid<>();
    grid.addColumn(MaskSample::name).setHeader("Name");
    grid.addColumn(MaskSample::number).setHeader("Number");
    grid.addColumn(MaskSample::mask).setHeader("Mask");
    grid.setItems(
        new MaskSample("Alice", "2005", "0000"),
        new MaskSample("Bob", "20", "00"),
        new MaskSample("Carol", "300", "000"));
    grid.setSelectionMode(SelectionMode.SINGLE);
    grid.setAllRowsVisible(true);
    grid.setId("set-mask-with-value-grid");

    // On selection, apply the row's value AND mask to both fields. Value is set
    // before the mask here so the previous (possibly shorter) mask is still the
    // active one when the value is applied - this is what truncates 2005 to 20.
    grid.addSelectionListener(ev -> ev.getFirstSelectedItem().ifPresent(sample -> {
      timeoutField.setValue(sample.number());
      timeoutMask.setMask(sample.mask());
      eagerField.setValue(sample.number());
      eagerMask.setMask(sample.mask());
    }));

    Paragraph info = new Paragraph("Select a row to set both the mask and the number on "
        + "the two fields. Try Bob (00 / 20) first, then Alice (0000 / 2005): the field "
        + "should show 2005, not 20. If it shows 20, the previous mask is still active "
        + "when the new value is applied.");

    add(createCard("Set mask + value from a Grid row", info, timeoutField, timeoutCommitted,
        eagerField, eagerCommitted, grid));
  }
}
