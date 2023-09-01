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

  connectedCallback() {
    super.connectedCallback();
    if (this.options && !this.imask) {
      if ('VAADIN-TEXT-FIELD' === this.parentElement.tagName.toUpperCase()) {
        this.imask = new IMask(this.parentElement, this._generateIMaskOptions(JSON.parse(this.options)));  
      } else {
        let el = this.parentElement.querySelector('input');
        this.imask = new IMask(el, this._generateIMaskOptions(JSON.parse(this.options)));
      }  
      this.parentElement.addEventListener("keydown", e => this._handleKeyEvent(e));
    }
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    if (this.imask) {
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

  _optionsChanged(newOptions, oldOptions) {
    if (!newOptions) {
      return;
    }

    this.options = newOptions;

    if (this.imask) {
      this.imask.destroy();
      this.imask = undefined;
    }

    if('VAADIN-TEXT-FIELD' === this.parentElement.tagName.toUpperCase()){
		  this.imask = new IMask(this.parentElement, this._generateIMaskOptions(JSON.parse(newOptions)));
    } else {
      let el = this.parentElement.querySelector('input');
      this.imask = new IMask(el, this._generateIMaskOptions(JSON.parse(newOptions)));		
    }      
    this.parentElement.addEventListener("keydown", e => this._handleKeyEvent(e));    
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
  
}

window.customElements.define(InputMask.is, InputMask);