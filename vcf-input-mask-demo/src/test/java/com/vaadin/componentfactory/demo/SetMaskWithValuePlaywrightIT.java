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
 * Verifies that applying a new mask together with a new value from a Grid row
 * renders the new value under the new mask, even when switching from a shorter
 * mask to a longer one.
 *
 * <p>Before the fix, selecting Bob (mask {@code 00}, value {@code 20}) and then
 * Alice (mask {@code 0000}, value {@code 2005}) rendered {@code 20}: the value
 * was applied while the previous, shorter mask was still active, truncating the
 * DOM, and the mask re-init then seeded IMask from the truncated DOM. The fix
 * re-seeds IMask from the field's intended value on a mask change.
 */
public class SetMaskWithValuePlaywrightIT extends AbstractPlaywrightTest {

  private static final String DEMO_PATH = "/set-mask-with-value";
  private static final String GRID = "vaadin-grid#set-mask-with-value-grid";
  private static final String TIMEOUT_INPUT =
      "vaadin-text-field#set-mask-with-value-timeout-field input";
  private static final String EAGER_INPUT =
      "vaadin-text-field#set-mask-with-value-eager-field input";

  @Test
  public void switchingFromShorterToLongerMaskRendersTheNewValue() {
    openDemo();

    // Bob: mask 00, value 20
    selectRow("Bob");
    assertThat(page.locator(TIMEOUT_INPUT)).hasValue("20");
    assertThat(page.locator(EAGER_INPUT)).hasValue("20");

    // Alice: mask 0000, value 2005 - must render 2005, not the truncated 20.
    selectRow("Alice");
    assertThat(page.locator(TIMEOUT_INPUT)).hasValue("2005");
    assertThat(page.locator(EAGER_INPUT)).hasValue("2005");
  }

  @Test
  public void switchingFromLongerToShorterMaskRendersTheNewValue() {
    openDemo();

    selectRow("Alice");
    assertThat(page.locator(TIMEOUT_INPUT)).hasValue("2005");

    selectRow("Bob");
    assertThat(page.locator(TIMEOUT_INPUT)).hasValue("20");
  }

  private void selectRow(String name) {
    page.locator(GRID).getByText(name,
        new Locator.GetByTextOptions().setExact(true)).click();
  }

  private void openDemo() {
    page.navigate(baseUrl() + DEMO_PATH);
    page.locator(GRID).waitFor(
        new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
  }
}
