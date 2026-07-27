/*
 * Copyright 2023-2026 Vaadin Ltd.
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
import { LitElement } from 'lit';
import IMask from 'imask';

/**
 * `input-mask` Web Component wrapper for IMask.js
 *
 */
class InputMask extends LitElement {
  static get is() { return 'input-mask'; }
  static get properties() {
    return {
      options: {
        type: Object
      },
      imask: {
        type: Object
      },
      allowWhitespace: {
        type: Boolean
      }
    };
  }

  get unmaskedValue() {
	return this.getUnmaskedValue();
  }

  set unmaskedValue(value) {}

  /** Initialize imask property */
  _initImask(){
	this._parentElement = this.parentElement;
	if (!this._parentElement) {
	  return;
	}
	if (['VAADIN-TEXT-FIELD', 'VAADIN-TEXT-AREA'].includes(this._parentElement.tagName.toUpperCase())) {
	  this.imask = new IMask(this._parentElement.inputElement, this._generateIMaskOptions(JSON.parse(this.options)));
	  this._boundHandleUnmaskedValueChange = this._handleUnmaskedValueChange.bind(this);
	  this._parentElement.addEventListener("change", this._boundHandleUnmaskedValueChange);

	  this._boundHandleInputValueChange = this._handleInputValueChange.bind(this);
	  this._maskedInputElement = this._parentElement.inputElement;
	  this._maskedInputElement.addEventListener("change", this._boundHandleInputValueChange);

	  // Registered after `new IMask(...)`, so it runs after IMask has reformatted
	  // the value on the same `input` event. Fixes live value-change modes
	  // (EAGER / TIMEOUT) where the host field otherwise captures the raw,
	  // pre-mask text and delivers characters the mask rejected to the server.
	  this._boundHandleMaskedInput = this._handleMaskedInput.bind(this);
	  this._maskedInputElement.addEventListener("input", this._boundHandleMaskedInput);

	} else {
	  const el = this._parentElement.querySelector('input');
	  this.imask = new IMask(el, this._generateIMaskOptions(JSON.parse(this.options)));
	  this._boundHandleInputMaskUnmaskedValueChanged = this._handleInputMaskUnmaskedValueChanged.bind(this);
	  this._parentElement.addEventListener("value-changed", this._boundHandleInputMaskUnmaskedValueChanged);
	}
	this._boundHandleKeyEvent = this._handleKeyEvent.bind(this);
	this._parentElement.addEventListener("keydown", this._boundHandleKeyEvent);
  }

  connectedCallback() {
    super.connectedCallback();
    if (this.options && !this.imask) {
	  this._initImask();
    }
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this._cleanUp();
  }

  /**
   * Re-initialise IMask whenever the `options` property is replaced after the
   * wrapper has been mounted. The first set is handled by `connectedCallback`
   * (oldValue is `undefined` in that case), so this only fires for subsequent
   * `setMask` calls coming from the server.
   */
  updated(changedProperties) {
    super.updated(changedProperties);
    if (changedProperties.has('options') && changedProperties.get('options') !== undefined) {
      this._cleanUp();
      if (this.options && this.isConnected) {
        this._initImask();
      }
    }
  }

  _cleanUp() {
	if (this.imask) {
	  if (this._parentElement) {
	    this._parentElement.removeEventListener("change", this._boundHandleUnmaskedValueChange);
	    this._parentElement.removeEventListener("value-changed", this._boundHandleInputMaskUnmaskedValueChanged);
	    this._parentElement.removeEventListener("keydown", this._boundHandleKeyEvent);
	  }
	  if (this._maskedInputElement) {
	    this._maskedInputElement.removeEventListener("change", this._boundHandleInputValueChange);
	    this._maskedInputElement.removeEventListener("input", this._boundHandleMaskedInput);
	    this._maskedInputElement = undefined;
	  }
	  this.imask.destroy();
	  this.imask = undefined;
	}
  }
  
  _handleKeyEvent(ev) {
    if (this.allowWhitespace) {
      return;
    }
    const spaceBar = ev.key == " " || ev.code == "Space" || ev.keyCode == 32;
    const selectAll = ev.target.selectionEnd > ev.target.selectionStart && ev.target.selectionEnd == ev.target.value.length;
    if (spaceBar && selectAll) {
      ev.preventDefault();
      this.parentElement.clear();
      this.imask.updateValue();
    } else if (spaceBar && ev.target.selectionEnd == 0) {
      ev.preventDefault();
   	}
  }
 
  /** Update textfield value on input update */
  _handleInputValueChange(e) {
	 this._parentElement.value = this.imask.value;
	 this._parentElement._onChange(e);
  }

  /**
   * Keep the host field's value in sync with the masked value during live
   * typing (value-change modes EAGER / TIMEOUT). The host field runs its own
   * `input` listener before IMask reformats, so it captures the raw, pre-mask
   * text; without this the server can receive a value that still contains
   * characters the mask rejected (e.g. an extra digit typed into an
   * already-full field).
   *
   * The correction is deferred to a microtask so it runs after the host field's
   * synchronous input handling has settled, and only for genuine user input:
   * the server-driven `setValue()` path updates IMask directly and its
   * programmatic (untrusted) input events must not be echoed back, or a value
   * pushed into the field from the server (e.g. a grid selection) could be
   * reverted to the previous value.
   */
  _handleMaskedInput(e) {
	if (!e.isTrusted) {
	  return;
	}
	queueMicrotask(() => {
	  if (this.imask
		  && this._maskedInputElement
		  && document.activeElement === this._maskedInputElement
		  && this._parentElement.value !== this.imask.value) {
		this._parentElement.value = this.imask.value;
	  }
	});
  }
 
  /** Update imask value on field "value-changed" event */ 	   
  _handleInputMaskUnmaskedValueChanged(ev) {    
	this.imask.value = ev.target.inputElement.value
	this.imask.updateValue();	
  }
  
  /** Handle imask's unmasked value */
  _handleUnmaskedValueChange(){
	const event = new CustomEvent("unmasked-value-changed", {
	    detail: this.imask.unmaskedValue,
	    composed: true,
	    cancelable: true,
	    bubbles: true
	});
	this.dispatchEvent(event);	
  }

  _generateIMaskOptions(maskOptions) {
    const result = {};
    maskOptions.forEach(opt => {
        if (opt.eval) {
          eval(`result.${opt.key} = ${opt.value}`);
        } else if (opt.key === 'blocks') {
          const blocks = {};
          opt.value.forEach(block => blocks[block.key] = this._parseBlock(block.value));
          result[opt.key] = blocks;
        } else {
          result[opt.key] = opt.value;
        }
    });
    return result;
  }

  _parseBlock(block) {
    const result = {};
    block.forEach(item => {
        if (item.eval) {
          eval(`result.${item.key} = ${item.value}`);
        } else {
          result[item.key] = item.value;
        }
    });
    return result;
  }

  getUnmaskedValue() {
    this._syncImaskFromInput();
    return this.imask ? this.imask.unmaskedValue : "";
  }

  getMaskedValue() {
    this._syncImaskFromInput();
    return this.imask ? this.imask.value : "";
  }

  /**
   * Ensure IMask reflects the host input's current value before a read. When the
   * host value is changed programmatically (e.g. a server-side setValue after a
   * grid selection), the listener that pushes the value into IMask may run after
   * an application getMaskedValue()/getUnmaskedValue() call, so the read would
   * otherwise return a stale, lag-by-one value. Re-syncing from the input here
   * makes reads consistent regardless of listener ordering. Only applies to the
   * text field / text area binding, where `_maskedInputElement` is the input.
   */
  _syncImaskFromInput() {
    const input = this._maskedInputElement;
    if (this.imask && input && this.imask.value !== input.value) {
      this.imask.value = input.value;
    }
  }
 
  setValue(value){
	if(this.imask) {
      this.imask.value = value;
    }
  }
  
}

window.customElements.define(InputMask.is, InputMask);