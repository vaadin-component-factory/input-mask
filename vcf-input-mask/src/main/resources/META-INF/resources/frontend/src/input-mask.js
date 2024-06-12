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
        type: Object,
        observer: '_optionsChanged'
      },
      imask: {
        type: Object
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
	if (['VAADIN-TEXT-FIELD', 'VAADIN-TEXT-AREA'].includes(this.parentElement.tagName.toUpperCase())) {
	  this.imask = new IMask(this.parentElement.inputElement, this._generateIMaskOptions(JSON.parse(this.options)));  
	  this._boundHandleUnmaskedValueChange = this._handleUnmaskedValueChange.bind(this);
	  this._parentElement.addEventListener("change", this._boundHandleUnmaskedValueChange);
	  
	  this._boundHandleInputValueChange = this._handleInputValueChange.bind(this);
	  this._parentElement.inputElement.addEventListener("change", this._boundHandleInputValueChange);
  
	} else {
	  const el = this.parentElement.querySelector('input');
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
  
  _cleanUp() {
	if (this.imask) {
	  this._parentElement.removeEventListener("change", this._boundHandleUnmaskedValueChange);
	  this._parentElement.removeEventListener("value-changed", this._boundHandleInputMaskUnmaskedValueChanged);  
	  this._parentElement.removeEventListener("keydown", this._boundHandleKeyEvent);
	  this.imask.destroy();
	  this.imask = undefined;
	}
  }
  
  _handleKeyEvent(ev) {
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

  _optionsChanged(newOptions, oldOptions) {
    if (!newOptions) {
      return;
    }

    this.options = newOptions;
    this._cleanUp();
	this._initImask();
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
    return this.imask ? this.imask.unmaskedValue : "";
  }

  getMaskedValue() {
    return this.imask ? this.imask.value : "";
  }
 
  setValue(value){
	if(this.imask) {
      this.imask.value = value;
    }
  }
  
}

window.customElements.define(InputMask.is, InputMask);