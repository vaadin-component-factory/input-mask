# Bug: pasting a formatted value into an `eager` mask shifts the constant characters into the editable slots

## Summary

When an `InputMask` is configured with the IMask option `eager: true` (or
`eager: 'append'`), pasting a fully formatted value into the field misplaces
the input: the mask's fixed characters contained in the pasted text are not
matched against the mask's own fixed characters, but are instead routed into
the editable slots. Typing, server-side `setValue`, and non-eager masks are
all unaffected.

## Reproduction

Mask with an embedded constant block (`\`-escaped characters are fixed
literals, unescaped `0` is a digit slot, `*` is an any-character slot):

```java
InputMask mask = new InputMask("*\\08\\0\\0\\0-00-**-000000-0",
    lazy(false), option("placeholderChar", " "), option("eager", true));
mask.extend(textField);
```

Paste the valid, fully formatted value into the empty field:

```
R08000-11-22-333333-4
```

| Scenario                                   | Result                  |
|--------------------------------------------|-------------------------|
| Paste, `eager: true`                       | `R08000-08-00-011223-3` (wrong) |
| Paste, `eager: false`                      | `R08000-11-22-333333-4` |
| Server-side value set, `eager: true`       | `R08000-11-22-333333-4` |
| Typing digit by digit, `eager: true`       | `R08000-11-22-333333-4` |

The wrong result is the pasted text with its constant block re-routed into the
editable slots: they receive `R`, `08`, `00`, `011223`, `3` — the fixed
characters `08000-` of the pasted string itself — and the remaining pasted
characters are dropped.

## Root cause

Upstream IMask behaviour, present in the pinned version (7.1.3) and unchanged
in the latest release. In `PatternFixedDefinition._appendChar`:

```js
const isResolved = appended && (this.isUnmasking || flags.input || flags.raw)
    && (!flags.raw || !appendEager) && !flags.tail;
```

Browser paste is processed with `raw: true`. With eager appending enabled,
`(!flags.raw || !appendEager)` is `false`, so a fixed character is
auto-inserted but never *consumes* the matching character from the pasted
text. Every fixed character of the pasted value therefore spills over into
the next editable slot.

The programmatic path (`imask.value = ...`, used by the wrapper for
server-side `setValue`) resolves with `{ input: true }` and no `raw` flag,
which is why only genuine user paste is affected.

## Fix (wrapper-level, `input-mask.js`)

Since the programmatic path handles fixed characters correctly even with
eager enabled, the wrapper intercepts the `paste` event and routes the
clipboard text through it, narrowly scoped to the broken case:

* only when the active mask has eager appending enabled
  (`eager: true` / `'append'`), and
* only when the paste replaces the whole field content — everything is
  selected, or nothing has been typed yet (`rawInputValue === ''`).

In that case the wrapper calls `preventDefault()`, applies the clipboard text
via `imask.value`, places the caret at the nearest input position after the
pasted content, and dispatches an `input` event so the host field's value
stays in sync. All other pastes (partial paste into the middle of an
already-filled eager field, any paste on a non-eager mask) keep the default
IMask behaviour.

## Regression coverage

`EmbeddedConstantMaskPlaywrightIT` covers, via real clipboard paste
(Ctrl/Cmd+V):

* pasting a formatted value into the empty eager field,
* pasting over an existing value with select-all,
* pasting into the non-eager (lazy) field, which exercises the untouched
  default path.
