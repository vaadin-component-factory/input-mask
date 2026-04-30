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

}
