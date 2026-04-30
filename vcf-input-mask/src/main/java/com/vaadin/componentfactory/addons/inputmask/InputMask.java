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
package com.vaadin.componentfactory.addons.inputmask;

import static com.vaadin.componentfactory.addons.inputmask.InputMaskOption.option;
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
@Tag(InputMask.TAG_NAME)
@NpmPackage(value = "imask", version = "7.1.3")
@JsModule("./src/input-mask.js")
public class InputMask extends AbstractSinglePropertyField<InputMask, String> implements HasValidation {

	private static final Logger logger = LoggerFactory.getLogger(InputMask.class);

    static final String TAG_NAME = "input-mask";

	private static final String ALLOW_WHITESPACE_PROPERTY = "allowWhitespace";

	private WeakReference<Component> extended;
	private Registration attachRegistration = null;
    private Registration valueChangeRegistration;
	private List<InputMaskOption> options;

	public InputMask(String mask, InputMaskOption... options) {
		this(mask, false, options);
	}

	/**
	 * Creates an {@code InputMask} with the given mask and IMask options.
	 *
	 * <p><strong>Security warning:</strong> when {@code evalMask} is
	 * {@code true}, the {@code mask} string is passed to JavaScript
	 * {@code eval()} on the client. Only pass developer-authored, trusted
	 * mask strings — never values coming from end-user input or any other
	 * untrusted source — or arbitrary JavaScript will run in the browser.
	 *
	 * @param mask
	 *            the mask pattern.
	 * @param evalMask
	 *            {@code true} to evaluate the mask as a JavaScript expression
	 *            (e.g. for regex masks like {@code "/^\\d+$/"} or built-ins
	 *            like {@code "Number"}). This value MUST NOT come from
	 *            untrusted input.
	 * @param options
	 *            additional IMask options.
	 */
	public InputMask(String mask, boolean evalMask, InputMaskOption... options) {
	    super("unmaskedValue", "", false);
		this.options = new ArrayList<>();
		this.options.add(option("mask", mask, evalMask));
		if (options != null) {
			this.options.addAll(Arrays.asList(options));
		}
	}

	/**
	 * Replaces the active mask while keeping every other option (such as
	 * {@code overwrite} or {@code lazy}) and the current binding to the host
	 * component. Safe to call before or after {@link #extend(Component)}; when
	 * the wrapper has already been extended onto a component, the new mask is
	 * pushed to the client and applied in place without recreating the
	 * {@code <input-mask>} element.
	 *
	 * @param mask
	 *            the new mask pattern, in the same format accepted by
	 *            {@link #InputMask(String, InputMaskOption...)}.
	 */
	public void setMask(String mask) {
		setMask(mask, false);
	}

	/**
	 * Replaces the active mask, optionally evaluating it as a JavaScript
	 * expression. The auxiliary options passed to the constructor are kept.
	 *
	 * <p><strong>Security warning:</strong> when {@code evalMask} is
	 * {@code true}, the {@code mask} string is passed to JavaScript
	 * {@code eval()} on the client. Only pass developer-authored, trusted
	 * mask strings — never values coming from end-user input or any other
	 * untrusted source — or arbitrary JavaScript will run in the browser.
	 *
	 * @param mask
	 *            the new mask pattern.
	 * @param evalMask
	 *            {@code true} to evaluate the mask string as a JavaScript
	 *            expression on the client (e.g. for regex masks like
	 *            {@code "/^\\d+$/"} or built-ins like {@code "Number"}),
	 *            {@code false} to send it as a plain string. This value MUST
	 *            NOT come from untrusted input.
	 * @see #setMask(String)
	 */
	public void setMask(String mask, boolean evalMask) {
		for (int i = 0; i < this.options.size(); i++) {
			if ("mask".equals(this.options.get(i).getKey())) {
				this.options.set(i, option("mask", mask, evalMask));
				pushOptionsToClient();
				return;
			}
		}
		this.options.add(0, option("mask", mask, evalMask));
		pushOptionsToClient();
	}

	/**
	 * Replaces the active mask and the auxiliary IMask options. The previous
	 * auxiliary options (e.g. {@code overwrite}, {@code lazy}) are discarded;
	 * only the supplied ones are applied alongside the new mask.
	 *
	 * <p><strong>Security warning:</strong> when {@code evalMask} is
	 * {@code true}, the {@code mask} string is passed to JavaScript
	 * {@code eval()} on the client. Only pass developer-authored, trusted
	 * mask strings — never values coming from end-user input or any other
	 * untrusted source — or arbitrary JavaScript will run in the browser.
	 *
	 * @param mask
	 *            the new mask pattern.
	 * @param evalMask
	 *            {@code true} to evaluate the mask as a JavaScript expression
	 *            on the client. This value MUST NOT come from untrusted input.
	 * @param options
	 *            replacement auxiliary options.
	 * @see #setMask(String)
	 */
	public void setMask(String mask, boolean evalMask, InputMaskOption... options) {
		this.options = new ArrayList<>();
		this.options.add(option("mask", mask, evalMask));
		if (options != null) {
			this.options.addAll(Arrays.asList(options));
		}
		pushOptionsToClient();
	}

	private void pushOptionsToClient() {
		if (extended == null || extended.get() == null) {
			return;
		}
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			getElement().setProperty("options", objectMapper.writeValueAsString(options));
		} catch (JacksonException ex) {
			logger.error("Error serializing InputMask options", ex);
		}
	}

	public void extend(Component component) {
	    extended = new WeakReference<Component>(component);
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

            Element componentElement = component.getElement();
            // remove any existing input-mask element attached to component
            componentElement.getChildren()
                    .filter(child -> TAG_NAME.equalsIgnoreCase(child.getTag()))
                    .findAny().ifPresent(componentElement::removeChild);
            componentElement.appendChild(getElement());
            
            if (HasValue.class.isAssignableFrom(component.getClass())) {
                valueChangeRegistration = HasValue.class.cast(component).addValueChangeListener(e -> {
                    if (!e.isFromClient()) {
                        getElement().executeJs("this.setValue($0.inputElement ? $0.inputElement.value : '')",
                                component.getElement());
                    }
                });
            }
        } catch (JacksonException ex) {
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

	/**
	 * Allows whitespace characters to be entered into the field from the keyboard
	 * or pasted from the clipboard.
	 * <p>
	 * When set to {@code false} (the default), the wrapper intercepts the space
	 * bar in two situations: pressing space when the caret is at position 0 is
	 * cancelled, and pressing space while the whole value is selected clears the
	 * field. When set to {@code true}, the wrapper does not intercept space, and
	 * IMask decides whether the character is accepted based on the configured
	 * mask.
	 *
	 * @param allowWhitespace
	 *            {@code true} to allow whitespace input, {@code false} to keep
	 *            the default behaviour.
	 */
	public void setAllowWhitespace(boolean allowWhitespace) {
		getElement().setProperty(ALLOW_WHITESPACE_PROPERTY, allowWhitespace);
	}

	/**
	 * @return {@code true} if whitespace input is currently allowed.
	 * @see #setAllowWhitespace(boolean)
	 */
	public boolean isAllowWhitespace() {
		return getElement().getProperty(ALLOW_WHITESPACE_PROPERTY, false);
	}

	@Override
    public void setErrorMessage(String errorMessage) {
        if (extendedHasValidation()) {
            ((HasValidation) extended.get()).setErrorMessage(errorMessage);
        }
    }

    @Override
    public String getErrorMessage() {
        return extendedHasValidation() ? ((HasValidation) extended.get()).getErrorMessage() : null;
    }

    @Override
    public void setInvalid(boolean invalid) {
        if (extendedHasValidation()) {
            ((HasValidation) extended.get()).setInvalid(invalid);
        }        
    }

    @Override
    public boolean isInvalid() {
        return extendedHasValidation() && ((HasValidation) extended.get()).isInvalid();
    }
    
    private boolean extendedHasValidation() {
        return extended != null && extended.get() != null && extended.get() instanceof HasValidation;
    }
  
    @Override
    protected void setPresentationValue(String newPresentationValue) {
      if (extended != null && extended.get() != null) {
        if (!TextField.class.isAssignableFrom(extended.get().getClass())) {
          throw new IllegalArgumentException(
              "Only TextField is supported for unmasked value binding.");
        } else {
          TextField.class.cast(extended.get()).setValue(newPresentationValue);
        }
      }
      super.setPresentationValue(newPresentationValue);
    }
}    