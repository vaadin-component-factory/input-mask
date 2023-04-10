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

import java.io.Serializable;

public class InputMaskOption implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String key;
	private final Object value;
	private final boolean eval;

	private InputMaskOption(String key, Object value) {
		this.key = key;
		this.value = value;
		this.eval = false;
	}

	private InputMaskOption(String key, Object value, boolean eval) {
		this.key = key;
		this.value = value;
		this.eval = eval;
	}

	public static InputMaskOption option(String key, Object value) {
		return new InputMaskOption(key, value);
	}

	public static InputMaskOption option(String key, Object value, boolean eval) {
		return new InputMaskOption(key, value, eval);
	}

	public static InputMaskOption option(String key, InputMaskOption... value) {
		return new InputMaskOption(key, value);
	}

	public static InputMaskOption blocks(InputMaskOption... value) {
		return new InputMaskOption("blocks", value);
	}

	public static InputMaskOption lazy(boolean value) {
		return option("lazy", value);
	}

	public static InputMaskOption overwrite(boolean value) {
		return option("overwrite", value);
	}

	/**
	 * Option that converts the input to uppercase.
	 * @return
	 */
	public static InputMaskOption toUppercase() {
		return new InputMaskOption("prepare", "str => str.toUpperCase()", true);
	}

	public String getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public boolean isEval() {
		return eval;
	}

}
