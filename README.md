# Taggable Text Area for Vaadin 14

Extension of text area component that supports tagging functionality. Users can insert tags by typing a "@" character to trigger a popup for selecting tags. Tags are rendered as clickable elements that can open popups for additional to add aditional information.

This component is part of Vaadin Component Factory.

## Compatibility

- Version 1.x.x -> Vaadin 14

## Running the component demo
Run from the command line:
- `mvn  -pl vcf-taggable-text-area-demo -Pwar install jetty:run`

Then navigate to `http://localhost:8080/`

## Installing the component
Run from the command line:
- `mvn clean install -DskipTests`

## Profiles
### Profile "directory"
This profile, when enabled, will create the zip file for uploading to Vaadin's directory

### Profile "production"
This profile, when enabled, will execute a production build for the demo

## Using the component in a Flow application
To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>org.vaadin.addons.componentfactory</groupId>
    <artifactId>vcf-taggable-text-area</artifactId>
    <version>${component.version}</version>
</dependency>
```

## How to Use
See examples on how to use at [demo](https://github.com/vaadin-component-factory/taggable-text-area/blob/main/vcf-taggable-text-area-demo/src/main/java/org/vaadin/addons/componentfactory/demo/TaggableTextAreaDemoView.java).

## Flow documentation
Documentation for Vaadin Flow can be found in [Flow documentation](https://vaadin.com/docs/latest/flow).

## License
Distributed under Apache Licence 2.0. 

### Sponsored development
Major pieces of development of this add-on has been sponsored by multiple customers of Vaadin. Read more about Expert on Demand at: [Support](https://vaadin.com/support) and [Pricing](https://vaadin.com/pricing).
