/*
 * Copyright 2023 Vaadin Ltd.
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
package com.vaadin.componentfactory.addons.inputmask;

import static com.vaadin.componentfactory.addons.inputmask.InputMaskOption.option;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag("input-mask")
@NpmPackage(value = "imask", version = "6.5.0")
@JsModule("./src/input-mask.js")
public class InputMask extends Component {

	private static final Logger logger = LoggerFactory.getLogger(InputMask.class);

	private WeakReference<Component> extended;
	private Registration attachRegistration = null;
	private List<InputMaskOption> options;

	public InputMask(String mask, InputMaskOption... options) {
		this(mask, false, options);
	}

	public InputMask(String mask, boolean evalMask, InputMaskOption... options) {
		this.options = new ArrayList<>();
		this.options.add(option("mask", mask, evalMask));
		if (options != null) {
			this.options.addAll(Arrays.asList(options));
		}
	}

	public void extend(Component component) {
		if (component.getUI().isPresent()) {
			extend(component, component.getUI().get());
		} else {
			attachRegistration = component.addAttachListener(event -> extend(component, event.getUI()));
			component.addDetachListener(event -> remove());
		}
	}

	private void extend(Component component, UI ui) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			getElement().setProperty("options", objectMapper.writeValueAsString(options));

			extended = new WeakReference<Component>(component);
			component.getElement().appendChild(getElement());			
			
			
		} catch (JsonProcessingException ex) {
			logger.error("Error serializing InputMask options", ex);
		}
	}

	public void remove() {
		if (attachRegistration != null) {
			attachRegistration.remove();
			attachRegistration = null;
		}
		if (extended != null) {
			getElement().removeFromParent();
			extended.clear();
		}
	}

	public void getUnmaskedValue(SerializableConsumer<String> consumer) {
        this.getElement().executeJs("return this.getUnmaskedValue()").then(String.class, value -> {
			consumer.accept(value);
		});
    }

	public void getMaskedValue(SerializableConsumer<String> consumer) {
        this.getElement().executeJs("return this.getMaskedValue()").then(String.class, value -> {
			consumer.accept(value);
		});
    }
}
