# Input Mask Add-on for Vaadin Flow

Vaadin Flow integration of [imaskjs library](https://www.npmjs.com/package/imask).

This component is part of Vaadin Component Factory.

## Description

InputMask component allows to add an input mask to a Vaadin Flow component like TextField or DatePicker.

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

### Basic use example
```
TextField phoneField = new TextField("Phone");
new InputMask("(000) 000-0000").extend(phoneField);
```

## License & Author

This Add-on is distributed under [Apache Licence 2.0](https://github.com/vaadin-component-factory/input-mask/blob/main/LICENSE).

InputMask Component for Vaadin Flow is written by Vaadin Ltd.

### Sponsored development
Major pieces of development of this add-on has been sponsored by multiple customers of Vaadin. Read more about Expert on Demand at: [Support](https://vaadin.com/support) and [Pricing](https://vaadin.com/pricing).

## Missing features or bugs
You can report any issue or missing feature on [GitHub](https://github.com/vaadin-component-factory/input-mask/issues).
