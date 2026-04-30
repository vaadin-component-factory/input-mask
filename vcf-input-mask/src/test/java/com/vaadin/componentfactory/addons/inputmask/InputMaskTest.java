/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.componentfactory.addons.inputmask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.TextField;
import net.jcip.annotations.NotThreadSafe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@NotThreadSafe
public class InputMaskTest {

	private UI ui;

	@BeforeEach
	public void setUp() {
		ui = new UI();
		UI.setCurrent(ui);
	}

	@AfterEach
	public void tearDown() {
		UI.setCurrent(null);
	}

	@Test
	public void inputMask_basicCases() {
		TextField textField = new TextField("");
		InputMask inputmask = new InputMask("(000)");
		inputmask.extend(textField);

		assertEquals("", textField.getValue());

		textField.setValue("(555)");
		assertEquals("(555)", textField.getValue());
	}

	@Test
	public void inputMask_allowWhitespace_defaultsToFalse() {
		InputMask inputMask = new InputMask("/^.*$/", true);

		assertFalse(inputMask.isAllowWhitespace(),
				"allowWhitespace should default to false to keep the existing behaviour");
		assertFalse(inputMask.getElement().getProperty("allowWhitespace", false),
				"allowWhitespace property should not be reflected to the element by default");
	}

	@Test
	public void inputMask_allowWhitespace_canBeToggled() {
		InputMask inputMask = new InputMask("/^.*$/", true);

		inputMask.setAllowWhitespace(true);
		assertTrue(inputMask.isAllowWhitespace());
		assertTrue(inputMask.getElement().getProperty("allowWhitespace", false),
				"allowWhitespace property should be reflected to the element when enabled");

		inputMask.setAllowWhitespace(false);
		assertFalse(inputMask.isAllowWhitespace());
		assertFalse(inputMask.getElement().getProperty("allowWhitespace", true),
				"allowWhitespace property should be reflected to the element when disabled");
	}

	@Test
	public void inputMask_allowWhitespace_persistsAfterExtend() {
		TextField textField = new TextField("");
		InputMask inputMask = new InputMask("/^.*$/", true);

		inputMask.setAllowWhitespace(true);
		inputMask.extend(textField);

		assertTrue(inputMask.isAllowWhitespace(),
				"allowWhitespace must remain enabled after extending a component");
		assertTrue(inputMask.getElement().getProperty("allowWhitespace", false),
				"allowWhitespace property should still be reflected to the element after extend");
	}

	@Test
	public void setMask_beforeExtend_appliesNewMaskOnExtend() {
		TextField textField = new TextField("");
		ui.add(textField);

		InputMask inputMask = new InputMask("(000)");
		inputMask.setMask("(00) 0000");
		inputMask.extend(textField);

		String options = inputMask.getElement().getProperty("options");
		assertTrue(options.contains("(00) 0000"),
				"options pushed to the client should reflect the most recent setMask call");
		assertFalse(options.contains("\"value\":\"(000)\""),
				"options pushed to the client should not still contain the original mask");
	}

	@Test
	public void setMask_afterExtend_updatesOptionsProperty() {
		TextField textField = new TextField("");
		ui.add(textField);

		InputMask inputMask = new InputMask("(000)");
		inputMask.extend(textField);

		String optionsBefore = inputMask.getElement().getProperty("options");
		assertTrue(optionsBefore.contains("(000)"),
				"original mask must be present in the options before setMask is called");

		inputMask.setMask("(00) 0000");

		String optionsAfter = inputMask.getElement().getProperty("options");
		assertTrue(optionsAfter.contains("(00) 0000"),
				"setMask must update the options pushed to the client");
		assertFalse(optionsAfter.contains("\"value\":\"(000)\""),
				"setMask must replace the previous mask entry, not append a second one");
	}

	@Test
	public void setMask_withOptions_replacesAuxiliaryOptions() {
		TextField textField = new TextField("");
		ui.add(textField);

		InputMask inputMask = new InputMask("(000)", false,
				InputMaskOption.option("overwrite", true));
		inputMask.extend(textField);

		inputMask.setMask("(00) 0000", false, InputMaskOption.option("lazy", false));

		String options = inputMask.getElement().getProperty("options");
		assertTrue(options.contains("(00) 0000"), "new mask must be present");
		assertTrue(options.contains("\"key\":\"lazy\""),
				"new auxiliary option must be present after setMask overload");
		assertFalse(options.contains("\"key\":\"overwrite\""),
				"setMask overload must replace the previous auxiliary options");
	}

	@Test
	public void setMask_afterExtend_doesNotChangeUnmaskedFieldValue() {
		TextField textField = new TextField("");
		ui.add(textField);

		InputMask inputMask = new InputMask("(000)");
		inputMask.extend(textField);
		textField.setValue("hello");

		inputMask.setMask("(00) 0000");

		assertEquals("hello", textField.getValue(),
				"setMask must not clear the field value - the host application owns it");
	}

}
