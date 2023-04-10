/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import static org.junit.Assert.assertEquals;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.TextField;
import net.jcip.annotations.NotThreadSafe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@NotThreadSafe
public class InputMaskTest {

	private UI ui;

	@Before
	public void setUp() {
		ui = new UI();
		UI.setCurrent(ui);
	}

	@After
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

}
