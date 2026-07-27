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

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Test;

/**
 * Verifies that selecting a Grid row on {@link BinderDemoView} populates the
 * Binder-bound phone field via a server-side {@code setValue}, and that the
 * subsequent {@code getUnmaskedValue()} read returns the value of the row just
 * selected - not a stale, lag-by-one value.
 *
 * <p>Before the fix, the add-on's IMask-sync listener (registered on attach,
 * after the application's value-change listener) ran after the application's
 * {@code getUnmaskedValue()} call, so the read returned the previously selected
 * row's unmasked value.
 */
public class BinderDemoPlaywrightIT extends AbstractPlaywrightTest {

  private static final String DEMO_PATH = "/binder-masked";
  private static final String GRID = "vaadin-grid#input-mask-on-text-field-on-grid-cell";
  private static final String MESSAGE = "#text-field-with-binder-demo-message";
  private static final String BOUND_FIELD_INPUT =
      "vaadin-text-field:has(input-mask) input";

  @Test
  public void selectingGridRowFillsFieldAndUnmaskedValueIsNotStale() {
    openDemo();

    // Select John Jackson -> (333) 898-9999
    page.locator(GRID).getByText("Jackson",
        new Locator.GetByTextOptions().setExact(true)).click();
    assertThat(page.locator(BOUND_FIELD_INPUT).first()).hasValue("(333) 898-9999");
    assertThat(page.locator(MESSAGE)).hasText(
        "Component value: (333) 898-9999 - Unmasked value: 3338989999");

    // Select Tom Ellison -> (333) 455-8978. The unmasked value must reflect
    // Tom, not lag behind at John's number.
    page.locator(GRID).getByText("Ellison",
        new Locator.GetByTextOptions().setExact(true)).click();
    assertThat(page.locator(BOUND_FIELD_INPUT).first()).hasValue("(333) 455-8978");
    assertThat(page.locator(MESSAGE)).hasText(
        "Component value: (333) 455-8978 - Unmasked value: 3334558978");
  }

  private void openDemo() {
    page.navigate(baseUrl() + DEMO_PATH);
    page.locator(GRID).waitFor(
        new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
  }
}
