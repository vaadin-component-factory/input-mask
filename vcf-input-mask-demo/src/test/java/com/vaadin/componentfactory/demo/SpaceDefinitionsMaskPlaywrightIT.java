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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Browser-level verification of the {@link SpaceDefinitionsMaskDemoView}: a
 * non-lazy mask with a space placeholder whose definitions also accept a
 * space, so the always-visible blank template is indistinguishable from a
 * value of typed spaces.
 *
 * <p>Covered regressions:
 * <ul>
 *   <li>A new IMask instance (re-created mask, or {@code setMask} on an
 *       empty field) must not read the previous instance's template back
 *       from the input as twenty typed spaces. That made the mask complete,
 *       so every keystroke was rejected.</li>
 *   <li>A mask applied to an already focused field must place the caret in
 *       the first editable slot instead of after the template.</li>
 * </ul>
 */
public class SpaceDefinitionsMaskPlaywrightIT extends AbstractPlaywrightTest {

  private static final String DEMO_PATH = "/space-definitions-mask";

  private static final String RECREATE_FIELD = "vaadin-text-field#space-definitions-recreate-text-field";
  private static final String SWITCH_FIELD = "vaadin-text-field#space-definitions-switch-text-field";
  private static final String FOCUS_FIELD = "vaadin-text-field#space-definitions-focus-text-field";

  private static final String TEMPLATE = "  -    -   -   -  -      ";
  private static final String SHORT_TEMPLATE = "  -    -   -  -      ";

  @Test
  public void recreatedMaskOnEmptyFieldStartsEmpty() {
    openDemo();
    markCurrentInputMask(RECREATE_FIELD);

    page.locator("#space-definitions-recreate-button").click();
    waitForNewImask(RECREATE_FIELD, SpaceDefinitionsMaskDemoView.MASK);

    Locator input = page.locator(RECREATE_FIELD + " input");
    assertThat(input).hasValue(TEMPLATE);
    assertEquals("", unmaskedValue(RECREATE_FIELD));

    input.click();
    input.pressSequentially("47IND8");
    assertThat(input).hasValue("47-IND8-   -   -  -      ");
  }

  @Test
  public void switchingMaskOfEmptyFieldStartsEmpty() {
    openDemo();
    markCurrentInputMask(SWITCH_FIELD);

    page.locator("#space-definitions-switch-button").click();
    waitForNewImask(SWITCH_FIELD, SpaceDefinitionsMaskDemoView.SHORT_MASK);

    Locator input = page.locator(SWITCH_FIELD + " input");
    assertThat(input).hasValue(SHORT_TEMPLATE);
    assertEquals("", unmaskedValue(SWITCH_FIELD));

    input.click();
    input.pressSequentially("47IND8");
    assertThat(input).hasValue("47-IND8-   -  -      ");
  }

  @Test
  public void maskAppliedToFocusedFieldPutsCaretInFirstSlot() {
    openDemo();
    markCurrentInputMask(FOCUS_FIELD);

    page.locator("#space-definitions-focus-button").click();
    waitForNewImask(FOCUS_FIELD, SpaceDefinitionsMaskDemoView.MASK);

    Locator input = page.locator(FOCUS_FIELD + " input");
    assertThat(input).hasValue(TEMPLATE);
    assertTrue((Boolean) input.evaluate("el => document.activeElement === el"),
        "the field must already be focused when the mask is applied");

    // Type without clicking: a click would let IMask align the caret itself.
    page.keyboard().type("47IND8");
    assertThat(input).hasValue("47-IND8-   -   -  -      ");
  }

  private void openDemo() {
    page.navigate(baseUrl() + DEMO_PATH);
    // The input-mask elements have no visible box, so wait for ATTACHED.
    page.locator(RECREATE_FIELD + " input-mask").waitFor(
        new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
    page.locator(SWITCH_FIELD + " input-mask").waitFor(
        new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
    page.locator("#space-definitions-focus-button").waitFor();
  }

  /** Remembers the field's current IMask instance (if any) to detect its replacement. */
  private void markCurrentInputMask(String fieldSelector) {
    page.evaluate("sel => { const m = document.querySelector(sel + ' input-mask');"
        + " window.__previousImask = m ? m.imask : undefined; }", fieldSelector);
  }

  /** Waits until a new IMask instance with the given mask is running on the field. */
  private void waitForNewImask(String fieldSelector, String mask) {
    page.waitForFunction("args => { const m = document.querySelector(args.sel + ' input-mask');"
        + " return m && m.imask && m.imask !== window.__previousImask"
        + " && m.imask.masked.mask === args.mask; }",
        Map.of("sel", fieldSelector, "mask", mask));
  }

  private String unmaskedValue(String fieldSelector) {
    return (String) page.evaluate(
        "sel => document.querySelector(sel + ' input-mask').imask.unmaskedValue", fieldSelector);
  }
}
