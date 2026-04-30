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
 * Browser-level verification of the {@code setMask} feature on the
 * {@link SetNewMaskDemoView}.
 *
 * <p>The view starts with a US-style phone mask. The tests:
 * <ul>
 *   <li>Type a value under the original mask and verify it is masked
 *       correctly.</li>
 *   <li>Switch the mask via the dropdown and verify (a) the dropdown change
 *       does not raise a client-side error, (b) typing under the new mask
 *       produces the expected new format.</li>
 * </ul>
 */
public class SetNewMaskPlaywrightIT extends AbstractPlaywrightTest {

  private static final String DEMO_PATH = "/set-new-mask";
  private static final String FIELD_INPUT_SELECTOR =
      "vaadin-text-field#set-new-mask-text-field input";
  private static final String INPUT_MASK_FOR_FIELD =
      "vaadin-text-field#set-new-mask-text-field input-mask";
  private static final String SELECT_SELECTOR =
      "vaadin-select#set-new-mask-select";

  private static final String WS_FIELD_INPUT_SELECTOR =
      "vaadin-text-field#set-new-mask-whitespace-text-field input";
  private static final String WS_INPUT_MASK_FOR_FIELD =
      "vaadin-text-field#set-new-mask-whitespace-text-field input-mask";
  private static final String WS_SELECT_SELECTOR =
      "vaadin-select#set-new-mask-whitespace-select";
  private static final String WS_TOGGLE_SELECTOR =
      "vaadin-checkbox#set-new-mask-whitespace-toggle";

  @Test
  public void initialMaskFormatsValueAsUsPhoneNumber() {
    openDemo();

    Locator input = page.locator(FIELD_INPUT_SELECTOR);
    input.click();
    input.pressSequentially("5551234567");
    assertThat(input).hasValue("(555) 123-4567");
  }

  @Test
  public void selectingNewMaskUpdatesActiveMaskAtRuntime() {
    openDemo();

    selectMask("+00 000 000 0000");

    Locator input = page.locator(FIELD_INPUT_SELECTOR);
    input.click();
    input.pressSequentially("358123456789");
    assertThat(input).hasValue("+35 812 345 6789");
  }

  @Test
  public void switchingMaskTwiceDoesNotBreakFormatting() {
    openDemo();

    // Type under the original mask
    Locator input = page.locator(FIELD_INPUT_SELECTOR);
    input.click();
    input.pressSequentially("5551234567");
    assertThat(input).hasValue("(555) 123-4567");

    // Switch to international format and type again - the demo clears the
    // field, so the previous value is gone before we type again.
    selectMask("+00 000 000 0000");
    input.click();
    input.pressSequentially("358123456789");
    assertThat(input).hasValue("+35 812 345 6789");

    // And switch back to the US mask
    selectMask("(000) 000-0000");
    input.click();
    input.pressSequentially("5551234567");
    assertThat(input).hasValue("(555) 123-4567");
  }

  @Test
  public void whitespaceToggleAppliesAfterMaskSwitch() {
    openDemo();

    Locator input = page.locator(WS_FIELD_INPUT_SELECTOR);

    // Default state: whitespace toggle OFF, regex mask /^.*$/. The wrapper
    // intercepts the leading space, so typing Space + a should yield "a".
    input.click();
    input.press("Space");
    input.pressSequentially("a");
    assertThat(input).hasValue("a");

    // Enable whitespace, then switch to the wildcard pattern mask. The demo
    // clears the field on mask change, so we get a clean slate.
    enableWhitespaceToggle();
    selectWhitespaceMask("*-00000000-a");

    // First mask slot is the IMask wildcard "*", which accepts a space when
    // the wrapper does not intercept it. Typing Space + 1 should produce
    // " -1" (wildcard space, literal "-", first digit).
    input.click();
    input.press("Space");
    input.pressSequentially("1");
    assertThat(input).hasValue(" -1");
  }

  private void openDemo() {
    page.navigate(baseUrl() + DEMO_PATH);
    // Wait until both input-mask wrappers are connected. The elements have no
    // visible box, so we wait for the ATTACHED state rather than VISIBLE.
    page.locator(INPUT_MASK_FOR_FIELD).waitFor(
        new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
    page.locator(WS_INPUT_MASK_FOR_FIELD).waitFor(
        new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
  }

  private void selectMask(String maskLabel) {
    selectMaskOn(SELECT_SELECTOR, INPUT_MASK_FOR_FIELD, maskLabel);
  }

  private void selectWhitespaceMask(String maskLabel) {
    selectMaskOn(WS_SELECT_SELECTOR, WS_INPUT_MASK_FOR_FIELD, maskLabel);
  }

  private void selectMaskOn(String selectSelector, String inputMaskSelector, String maskLabel) {
    // Open the Vaadin Select overlay and click the matching item. The dropdown
    // items render inside vaadin-select-list-box, so scope the lookup there to
    // avoid colliding with the copy of the selected item that Vaadin renders
    // inside the value button. The select round-trips through the server, so
    // wait until the wrapper has actually re-initialised IMask with the new
    // mask before continuing.
    page.locator(selectSelector + " vaadin-select-value-button").click();
    page.locator("vaadin-select-list-box vaadin-select-item")
        .getByText(maskLabel, new com.microsoft.playwright.Locator.GetByTextOptions().setExact(true))
        .click();
    page.waitForFunction(
        "args => {"
            + "  const el = document.querySelector(args.selector);"
            + "  if (!el || !el.imask) return false;"
            + "  const opts = JSON.parse(el.options || '[]');"
            + "  const maskOpt = opts.find(o => o.key === 'mask');"
            + "  return maskOpt && maskOpt.value === args.label;"
            + "}",
        java.util.Map.of("selector", inputMaskSelector, "label", maskLabel));
  }

  private void enableWhitespaceToggle() {
    page.locator(WS_TOGGLE_SELECTOR).click();
    // Toggle round-trips through the server before the property flips on the
    // wrapper. Wait until the change is visible on the client.
    page.waitForFunction(
        "selector => document.querySelector(selector).allowWhitespace === true",
        WS_INPUT_MASK_FOR_FIELD);
  }
}
