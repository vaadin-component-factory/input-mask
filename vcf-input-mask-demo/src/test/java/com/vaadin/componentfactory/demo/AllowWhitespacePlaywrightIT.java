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
 * Browser-level verification of the {@code allowWhitespace} feature on the
 * {@link AllowWhitespaceDemoView} text field.
 *
 * <p>The first card on the demo view defaults to {@code allowWhitespace=false},
 * so:
 * <ul>
 *   <li>Pressing the space bar while the caret is at position 0 must be
 *       swallowed by the wrapper (typing {@code Space + a} leaves the field
 *       with the value {@code "a"}).</li>
 *   <li>After ticking the toggle, the same key sequence must produce
 *       {@code " a"} because the wrapper no longer intercepts the space.</li>
 * </ul>
 */
public class AllowWhitespacePlaywrightIT extends AbstractPlaywrightTest {

  private static final String DEMO_PATH = "/allow-whitespace";
  private static final String FIELD_INPUT_SELECTOR =
      "vaadin-text-field#allow-whitespace-on-text-field input";
  private static final String TOGGLE_SELECTOR =
      "vaadin-checkbox#allow-whitespace-on-text-field-toggle";
  private static final String INPUT_MASK_FOR_FIELD =
      "vaadin-text-field#allow-whitespace-on-text-field input-mask";

  @Test
  public void leadingWhitespaceIsBlockedByDefault() {
    openDemo();

    Locator input = page.locator(FIELD_INPUT_SELECTOR);
    input.click();
    input.press("Space");
    input.pressSequentially("a");
    assertThat(input).hasValue("a");
  }

  @Test
  public void leadingWhitespaceIsAllowedWhenFeatureEnabled() {
    openDemo();
    enableAllowWhitespace();

    Locator input = page.locator(FIELD_INPUT_SELECTOR);
    input.click();
    input.press("Space");
    input.pressSequentially("a");
    assertThat(input).hasValue(" a");
  }

  private void openDemo() {
    page.navigate(baseUrl() + DEMO_PATH);
    // Wait until the input-mask wrapper is connected: by then the keydown
    // listener that implements the feature is attached, so the test no longer
    // races the bootstrap. The element itself has no visible box, so we wait
    // for the ATTACHED state rather than the default VISIBLE.
    page.locator(INPUT_MASK_FOR_FIELD).waitFor(
        new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
  }

  private void enableAllowWhitespace() {
    page.locator(TOGGLE_SELECTOR).click();
    // The toggle round-trips through the server before flipping the property
    // on the wrapper. Wait for the property to be true before typing.
    page.waitForFunction(
        "() => document.querySelector('" + INPUT_MASK_FOR_FIELD + "').allowWhitespace === true");
  }
}
