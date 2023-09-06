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
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(InputMask.TAG_NAME)
@NpmPackage(value = "imask", version = "7.1.3")
@JsModule("./src/input-mask.js")
public class InputMask extends Component {

	private static final Logger logger = LoggerFactory.getLogger(InputMask.class);

    static final String TAG_NAME = "input-mask";

	private WeakReference<Component> extended;
	private Registration attachRegistration = null;
    private Registration valueChangeRegistration;
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

    @SuppressWarnings("unchecked")
    private void extend(Component component, UI ui) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            getElement().setProperty("options", objectMapper.writeValueAsString(options));

            extended = new WeakReference<Component>(component);
            
            Element componentElement = component.getElement();
            // remove any existing input-mask element attached to component
            componentElement.getChildren()
                    .filter(child -> TAG_NAME.equalsIgnoreCase(child.getTag()))
                    .findAny().ifPresent(componentElement::removeChild);
            componentElement.appendChild(getElement());
            
            if (HasValue.class.isAssignableFrom(component.getClass())) {
                valueChangeRegistration = HasValue.class.cast(component).addValueChangeListener(e -> {
                    if (!e.isFromClient()) {
                        getElement().executeJs("this.setValue($0)",
                                e.getValue() == null ? "" : e.getValue().toString());
                    }
                });
            }
        } catch (JsonProcessingException ex) {
            logger.error("Error serializing InputMask options", ex);
        }
    }

    public void remove() {
        if (attachRegistration != null) {
            attachRegistration.remove();
            attachRegistration = null;
        }
        if (valueChangeRegistration != null) {
            valueChangeRegistration.remove();
            valueChangeRegistration = null;
        }
        if (extended != null) {
            getElement().removeFromParent();
            extended.clear();
        }
        extended = null;
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
