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
import { LitElement } from 'lit-element';
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
    
    let parentElement;
        
    if (this.options && !this.imask) {
      let el = this.parentElement.shadowRoot.querySelector('input');
      if(el) {
        this.imask = new IMask(this.parentElement, this._generateIMaskOptions(JSON.parse(this.options)));
      } else {
        el = this.parentElement.shadowRoot.querySelector('#input');
        this.imask = new IMask(el, this._generateIMaskOptions(JSON.parse(this.options))); 
      }
      el.addEventListener("keydown", e => this._handleKeyEvent(e)); 
      
      // need to keep track of parent element to be able to re-set caret position later
      parentElement = this.parentElement;
    }      
    
    if(this.imask) {
		// override _onInput function and re-set real caret position from imask
		let customOnInput = this.imask._onInput;
	    this.imask._onInput = function (e) {
			customOnInput(e);
			let imaskSelectionStart = parentElement.querySelector("input-mask").__imask.selectionStart;			
			e.target.inputElement.setSelectionRange(imaskSelectionStart, imaskSelectionStart);			
		}	
		
		this.imask.el.bindEvents({
	      input: this.imask._onInput,
	    }); 
		
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
	const backspace = ev.code == "Backspace" || ev.keyCode == 8;
    const spaceBar = ev.key == " " || ev.code == "Space" || ev.keyCode == 32;
    const selectAll = ev.target.selectionEnd > ev.target.selectionStart && ev.target.selectionEnd == ev.target.value.length;
    if (spaceBar && selectAll) {
      ev.preventDefault();
      this.parentElement.clear();
      this.imask.updateValue();
    } else if (spaceBar && ev.target.selectionEnd == 0) {
      ev.preventDefault();
    } else if (backspace && !this.imask.masked.lazy) {
	  if (this.imask.masked._blocks.at(ev.target.selectionStart-1).isFixed) {
		var i = 1;
		while(this.imask.masked._blocks.at(ev.target.selectionStart-i).isFixed){
			i++;
		}	
		ev.target.setSelectionRange(ev.target.selectionStart-i, ev.target.selectionEnd-i);
	  }
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

    let el = this.parentElement.shadowRoot.querySelector('input');
    if(el) {
      this.imask = new IMask(this.parentElement, this._generateIMaskOptions(JSON.parse(this.options)));
    } else {
      el = this.parentElement.shadowRoot.querySelector('#input');
      this.imask = new IMask(el, this._generateIMaskOptions(JSON.parse(this.options))); 
    }
    el.addEventListener("keydown", e => this._handleKeyEvent(e));      
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