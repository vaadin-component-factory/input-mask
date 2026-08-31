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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Browser-level verification of the {@link EmbeddedConstantMaskDemoView}
 * use-case: a legacy-style mask {@code " 08000-  -  -      - "} where the
 * digits {@code 08000} and the hyphens are constant and the blanks are
 * editable slots, including one editable slot <em>before</em> the constant
 * block (IMask pattern {@code *\08\0\0\0-00-**-000000-0}).
 *
 * <p>Covered behaviour:
 * <ul>
 *   <li>The caret initially sits in the empty slot before {@code 08000}, and
 *       after the first typed character the eager option moves it over the
 *       constant block to the next editable slot.</li>
 *   <li>Typing fills the hyphen-separated slots, skipping the literals.</li>
 *   <li>Pasting a fully formatted value (Ctrl/Cmd+V) keeps the constant
 *       characters of the pasted text aligned with the mask literals instead
 *       of shifting them into the editable slots - the regression fixed in
 *       the input-mask wrapper for eager masks.</li>
 * </ul>
 */
public class EmbeddedConstantMaskPlaywrightIT extends AbstractPlaywrightTest {

  private static final String DEMO_PATH = "/embedded-constant-mask";

  private static final String LEGACY_FIELD_INPUT_SELECTOR =
      "vaadin-text-field#embedded-constant-mask-text-field input";
  private static final String LEGACY_INPUT_MASK_SELECTOR =
      "vaadin-text-field#embedded-constant-mask-text-field input-mask";
  private static final String LEGACY_MESSAGE_SELECTOR =
      "#embedded-constant-mask-demo-message";

  private static final String LAZY_FIELD_INPUT_SELECTOR =
      "vaadin-text-field#embedded-constant-mask-lazy-text-field input";
  private static final String LAZY_INPUT_MASK_SELECTOR =
      "vaadin-text-field#embedded-constant-mask-lazy-text-field input-mask";
  private static final String LAZY_MESSAGE_SELECTOR =
      "#embedded-constant-mask-lazy-demo-message";

  private static final String EAGER_VCM_FIELD_INPUT_SELECTOR =
      "vaadin-text-field#embedded-constant-mask-eager-vcm-text-field input";
  private static final String EAGER_VCM_INPUT_MASK_SELECTOR =
      "vaadin-text-field#embedded-constant-mask-eager-vcm-text-field input-mask";
  private static final String EAGER_VCM_MESSAGE_SELECTOR =
      "#embedded-constant-mask-eager-vcm-demo-message";

  // Mask template rendered by lazy=false + placeholderChar " ":
  // one blank slot, constant "08000-", then hyphen-separated slot groups.
  private static final String EMPTY_TEMPLATE = " 08000-  -  -      - ";
  private static final String TEMPLATE_AFTER_FIRST_CHAR = "108000-  -  -      - ";
  private static final String TEMPLATE_AFTER_TYPING_12345 = "108000-23-45-      - ";

  // A complete, correctly formatted value and its unmasked form.
  private static final String FORMATTED_VALUE = "R08000-11-22-333333-4";
  private static final String UNMASKED_VALUE = "R11223333334";

  // Position right after the constant block "108000-", i.e. the second
  // editable slot of the mask.
  private static final int POSITION_AFTER_CONSTANT_BLOCK = 7;

  private static final double CARET_ASSERT_TIMEOUT_MS = 5_000;

  @Test
  public void caretStartsInLeadingSlotAndSkipsConstantBlock() {
    openDemo();

    Locator input = page.locator(LEGACY_FIELD_INPUT_SELECTOR);
    // lazy=false renders the whole template as soon as IMask initializes
    assertThat(input).hasValue(EMPTY_TEMPLATE);

    // Clicking the empty field must land the caret in the editable slot
    // before the constant block, i.e. position 0. IMask re-aligns the caret
    // asynchronously after the click, so poll instead of reading once.
    input.click();
    waitForCaretAt(LEGACY_FIELD_INPUT_SELECTOR, 0);

    // The first typed character fills the leading slot and, thanks to the
    // eager option, the caret jumps over the constant "08000-" to the next
    // editable slot immediately.
    input.pressSequentially("1");
    assertThat(input).hasValue(TEMPLATE_AFTER_FIRST_CHAR);
    waitForCaretAt(LEGACY_FIELD_INPUT_SELECTOR, POSITION_AFTER_CONSTANT_BLOCK);

    // The remaining digits fill the hyphen-separated slots, with the literal
    // hyphens skipped automatically.
    input.pressSequentially("2345");
    assertThat(input).hasValue(TEMPLATE_AFTER_TYPING_12345);
  }

  @Test
  public void typingFillsSlotsAcrossConstantsInLazyMode() {
    openDemo();

    Locator input = page.locator(LAZY_FIELD_INPUT_SELECTOR);
    input.click();
    input.pressSequentially("12345");
    assertThat(input).hasValue("108000-23-45");
  }

  @Test
  public void unmaskedValueExcludesConstantBlockAndHyphens() {
    openDemo();

    Locator input = page.locator(LAZY_FIELD_INPUT_SELECTOR);
    input.click();
    input.pressSequentially("12345");
    // Blur to fire the value-change listener that renders the masked and
    // unmasked values into the message div.
    input.press("Tab");

    Locator message = page.locator(LAZY_MESSAGE_SELECTOR);
    assertThat(message)
        .hasText("Masked value: 108000-23-45 - Unmasked value: 12345");
  }

  @Test
  public void pastingFormattedValueIntoEmptyEagerFieldKeepsSlotAlignment() {
    openDemo();

    Locator input = page.locator(LEGACY_FIELD_INPUT_SELECTOR);
    input.click();
    paste(FORMATTED_VALUE);

    // Without the wrapper's paste handling, eager mode routes the constant
    // characters of the pasted text into the editable slots, producing
    // "R08000-08-00-011223-3" instead.
    assertThat(input).hasValue(FORMATTED_VALUE);
    // Caret lands after the pasted content.
    waitForCaretAt(LEGACY_FIELD_INPUT_SELECTOR, FORMATTED_VALUE.length());

    // The wrapper dispatches the change flow itself (the prevented paste
    // never marks the input dirty), so the server receives the value without
    // an explicit blur.
    Locator message = page.locator(LEGACY_MESSAGE_SELECTOR);
    assertThat(message).hasText(
        "Masked value: " + FORMATTED_VALUE + " - Unmasked value: " + UNMASKED_VALUE);
  }

  @Test
  public void pastingFormattedValueOverSelectAllReplacesPreviousValue() {
    openDemo();

    Locator input = page.locator(LEGACY_FIELD_INPUT_SELECTOR);
    input.click();
    input.pressSequentially("12345");
    assertThat(input).hasValue(TEMPLATE_AFTER_TYPING_12345);

    // Select-all before pasting over the existing value. Locator.selectText()
    // is used instead of pressing ControlOrMeta+a: on macOS the synthesized
    // key event does not trigger the browser's native select-all command.
    input.selectText();
    paste(FORMATTED_VALUE);
    assertThat(input).hasValue(FORMATTED_VALUE);
  }

  @Test
  public void pastingIntoEagerValueChangeModeFieldFiresValueChangeBeforeBlur() {
    openDemo();

    Locator input = page.locator(EAGER_VCM_FIELD_INPUT_SELECTOR);
    input.click();
    paste(FORMATTED_VALUE);
    assertThat(input).hasValue(FORMATTED_VALUE);

    // ValueChangeMode.EAGER synchronizes on the input event, so the value
    // change event must reach the server without any blur. The wrapper
    // replays the input event after applying the pasted value; without that,
    // the prevented paste produces no input event and the server never
    // receives a value change (the regression behind this test).
    Locator message = page.locator(EAGER_VCM_MESSAGE_SELECTOR);
    assertThat(message).hasText("[value-change: " + FORMATTED_VALUE + "]");

    // Blurring afterwards must append the blur event after the value change,
    // the ordering that applications combining value change and blur
    // listeners rely on.
    input.press("Tab");
    assertThat(message).hasText("[value-change: " + FORMATTED_VALUE + "][blur]");
  }

  @Test
  public void pastingFormattedValueIntoNonEagerFieldUsesDefaultHandling() {
    openDemo();

    // The lazy field has no eager option, so the wrapper leaves the paste to
    // IMask's default handling, which consumes matching fixed characters.
    Locator input = page.locator(LAZY_FIELD_INPUT_SELECTOR);
    input.click();
    paste(FORMATTED_VALUE);
    assertThat(input).hasValue(FORMATTED_VALUE);
  }

  private void openDemo() {
    page.navigate(baseUrl() + DEMO_PATH);
    // Wait until both input-mask wrappers are connected. The elements have no
    // visible box, so wait for the ATTACHED state rather than VISIBLE.
    page.locator(LEGACY_INPUT_MASK_SELECTOR).waitFor(
        new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
    page.locator(LAZY_INPUT_MASK_SELECTOR).waitFor(
        new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
    page.locator(EAGER_VCM_INPUT_MASK_SELECTOR).waitFor(
        new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
  }

  /**
   * Performs a genuine clipboard paste (Ctrl/Cmd+V) of the given text into the
   * currently focused element, so the wrapper's {@code paste} event handling
   * is exercised - unlike {@code Keyboard.insertText}, which bypasses the
   * paste event entirely.
   */
  private void paste(String text) {
    context.grantPermissions(List.of("clipboard-read", "clipboard-write"));
    page.evaluate("text => navigator.clipboard.writeText(text)", text);
    page.keyboard().press("ControlOrMeta+v");
  }

  private void waitForCaretAt(String inputSelector, int expectedPosition) {
    page.waitForFunction(
        "args => {"
            + "  const el = document.querySelector(args.selector);"
            + "  return el && el.selectionStart === args.position"
            + "      && el.selectionEnd === args.position;"
            + "}",
        java.util.Map.of("selector", inputSelector, "position", expectedPosition),
        new Page.WaitForFunctionOptions().setTimeout(CARET_ASSERT_TIMEOUT_MS));
  }
}
