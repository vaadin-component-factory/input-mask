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
 * Regression tests for the masked-value sync fix on {@link MaskedValueSyncDemoView}.
 *
 * <p>Before the fix, with a live {@code ValueChangeMode} (EAGER / TIMEOUT) the host
 * field captured the raw, pre-mask text on {@code input} - it ran its own listener
 * before IMask reformatted - so the value delivered to the server could contain a
 * character the mask rejected (for example an extra digit typed into an already-full
 * field). These tests type one digit past a full phone mask and assert that both the
 * field and the server-committed value stay masked, and that a value pushed into the
 * field from the server (via a grid selection) is not disturbed by the sync.
 */
public class MaskedValueSyncPlaywrightIT extends AbstractPlaywrightTest {

  private static final String DEMO_PATH = "/masked-value-sync";

  private static final String FIELD_INPUT = "vaadin-text-field#masked-value-sync-field input";
  private static final String FIELD_MASK = "vaadin-text-field#masked-value-sync-field input-mask";
  private static final String COMMITTED = "#masked-value-sync-committed";
  private static final String UNMASKED = "#masked-value-sync-unmasked";
  private static final String GRID = "vaadin-grid#masked-value-sync-grid";

  private static final String EAGER_INPUT =
      "vaadin-text-field#masked-value-sync-eager-field input";
  private static final String EAGER_COMMITTED = "#masked-value-sync-eager-committed";

  @Test
  public void typingPastFullMaskKeepsTimeoutValueMasked() {
    openDemo();

    Locator input = page.locator(FIELD_INPUT);
    input.click();
    // Eleven digits: one more than the ten-digit phone mask can hold. Use a
    // human-like delay so TIMEOUT mode debounces per keystroke as it would in
    // real use.
    input.pressSequentially("12345678901",
        new Locator.PressSequentiallyOptions().setDelay(120));

    assertThat(input).hasValue("(123) 456-7890");
    // The value delivered to the server is the masked value, not "12345678901".
    assertThat(page.locator(COMMITTED)).hasText("getValue()=\"(123) 456-7890\"");
  }

  @Test
  public void typingPastFullMaskKeepsEagerValueMasked() {
    openDemo();

    Locator input = page.locator(EAGER_INPUT);
    input.click();
    input.pressSequentially("12345678901",
        new Locator.PressSequentiallyOptions().setDelay(120));

    assertThat(input).hasValue("(123) 456-7890");
    assertThat(page.locator(EAGER_COMMITTED)).hasText("getValue()=\"(123) 456-7890\"");
  }

  @Test
  public void gridSelectionPopulatesFieldWithoutBeingReverted() {
    openDemo();

    // Selecting a row pushes the raw phone number into the field from the server.
    selectRow("Bob");

    Locator input = page.locator(FIELD_INPUT);
    // The programmatic value is displayed masked and is not reverted to a previous
    // value by the user-input sync.
    assertThat(input).hasValue("(555) 987-6543");
    assertThat(page.locator(COMMITTED)).hasText("getValue()=\"5559876543\"");
  }

  @Test
  public void gridSelectionUnmaskedValueIsNotStale() {
    openDemo();

    // Select two rows in turn. getUnmaskedValue() runs from the field's
    // value-change listener; after a programmatic setValue it must reflect the
    // row just selected, not lag one selection behind.
    selectRow("Alice");
    assertThat(page.locator(UNMASKED)).hasText("getUnmaskedValue()=\"1234567890\"");

    selectRow("Bob");
    assertThat(page.locator(FIELD_INPUT)).hasValue("(555) 987-6543");
    assertThat(page.locator(UNMASKED)).hasText("getUnmaskedValue()=\"5559876543\"");
  }

  private void selectRow(String name) {
    page.locator(GRID).getByText(name,
        new Locator.GetByTextOptions().setExact(true)).click();
  }

  private void openDemo() {
    page.navigate(baseUrl() + DEMO_PATH);
    page.locator(FIELD_MASK).waitFor(
        new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
  }
}
