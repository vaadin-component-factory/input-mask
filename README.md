# Input Mask Add-on for Vaadin Flow

Vaadin Flow integration of [imaskjs library](https://www.npmjs.com/package/imask).

This component is part of Vaadin Component Factory.

## Description

InputMask component allows to add an input mask to a Vaadin Flow component like TextField or DatePicker.

Go to [How to use](#Howtouse) section for use examples.

## Development instructions

Build the project and install the add-on locally:
```
mvn clean install
```
To start the demo server go to vcf-input-mask-demo and run:
```
mvn jetty:run
```

This deploys demo at http://localhost:8080

## Using the component in a Flow application
To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>org.vaadin.addons.componentfactory</groupId>
    <artifactId>vcf-input-mask</artifactId>
    <version>${component.version}</version>
</dependency>
```

## How to use

### Basic use example
```java
TextField phoneField = new TextField("Phone");
new InputMask("(000) 000-0000").extend(phoneField);
```

```java
DatePicker dateField = new DatePicker("Date");
dateField.setI18n(new DatePickerI18n().setDateFormat("MM/dd/yyyy"));
InputMask dateFieldMask = new InputMask("00/00/0000");
dateFieldMask.extend(dateField);
```

### Binder example

```java
TextField phoneField = new TextField("Phone");
InputMask phoneFieldMask = new InputMask("(000) 000-0000");
phoneFieldMask.extend(phoneField);

Binder<Person> binder = new Binder<Person>();    
binder.forField(phoneField).withNullRepresentation("")
	.withValidator(this::validatePhone)
	.bind(Person::getPhone, Person::setPhone);

Person person = new Person();
person.setPhone("1112223333");
binder.setBean(person);
```

```java
DatePicker dateField = new DatePicker("Date");
dateField.setI18n(new DatePickerI18n().setDateFormat("MM/dd/yyyy"));
InputMask dateFieldMask = new InputMask(DATE_MASK, option("overwrite", true));    
dateFieldMask.extend(dateField);

Binder<Person> binder = new Binder<>();
binder.forField(dateField)
	.withValidator(this::validateBirthday)
	.bind(Person::getBirthday, Person::setBirthday);
binder.readBean(new Person());
```

### Special use case: binding unmasked value (text field only)

In order to allow binder to use the unmasked value from the InputMask, binder should be defined using the InputMask field instead of the actual text field.
This use case is only supported for TextField (IllegalArgumentException is thrown if this approach is intended to be used with a different field).

```java
TextField phoneField = new TextField("Phone");

InputMask phoneFieldMask = new InputMask("(000) 000-0000");
phoneFieldMask.extend(phoneField);

Binder<Person> binder = new Binder<Person>();   

// use input mask field for the binding so the unsmasked value 
// is linked to binder (phoneFieldMask) 
binder.forField(phoneFieldMask)
	.withValidator(this::validatePhone)
	.bind(Person::getPhone, Person::setPhone);

binder.setBean(new Person());
```

### Allowing whitespace input

By default, the wrapper intercepts the space bar in two situations: pressing space when the caret is at position 0 is cancelled, and pressing space while the whole value is selected clears the field. This makes the component unsuitable for masks where a space is a legitimate part of the value (for example a name, a sentence, or a pattern with the IMask `*` wildcard).

Call `setAllowWhitespace(true)` to opt in to whitespace input. When enabled, the wrapper does not intercept the space bar and lets IMask decide whether to accept the character based on the configured mask. The same applies to clipboard paste: spaces are kept when the mask permits them.

```java
TextField referenceField = new TextField("Reference code");
InputMask referenceMask = new InputMask("*-00000000-a");
referenceMask.extend(referenceField);

// the first character of the mask ("*") accepts any character,
// including a whitespace, once the toggle below is on
referenceMask.setAllowWhitespace(true);
```

The default value is `false`, so existing applications that rely on the "select-all + space = clear" shortcut keep their current behaviour.

## License & Author

This Add-on is distributed under [Apache Licence 2.0](https://github.com/vaadin-component-factory/input-mask/blob/main/LICENSE).

InputMask Component for Vaadin Flow is written by Vaadin Ltd.

### Sponsored development
Major pieces of development of this add-on has been sponsored by multiple customers of Vaadin. Read more about Expert on Demand at: [Support](https://vaadin.com/support) and [Pricing](https://vaadin.com/pricing).

## Missing features or bugs
You can report any issue or missing feature on [GitHub](https://github.com/vaadin-component-factory/input-mask/issues).
