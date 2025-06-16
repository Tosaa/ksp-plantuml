# PumlKSP 
**(Work in progress, unreleased!)**

PumlKSP is designed to configure and create Class Diagrams in the plantuml format, by applying a KSP Plugin on your Project.

## Try it out
Run `./gradlew :app:kspKotlin` to generate a Plantuml Classdiagram.  
The output can be found at `app/build/resources/main/ClassDiagram.puml`.  
It can be rendered by using the Intellij Plantuml Plugin or the [Plantuml.com](https://plantuml.com/)

*** 

## Features
### Visualize inheritance, relations and dependencies
The by PumlKSP generated UML diagrams you can visualize 
the inheritance hierarchy of your classes, 
showcasing the parent-child relationships and 
enabling you to identify dependencies within your classes.

### Filter classes, functions and variables according their visibility
PumlKSP provides granular control over visibility modifiers. 
For example one can focus on the public interface of the codebase by disabling private and internal classes, variables and functions.
If one wants to dig deeper into internal or private implementation details all visibility modifiers can be enabled.

you can individually enable or disable the visibility of:
- Public classes, functions, and variables
- Internal classes, functions, and variables
- Private classes, functions, and variables

### Kotlin-Specific Support for Accurate UML

PumlKSP is taking into account Kotlin's unique features and language nuances.
When generating UML diagrams, it is ensured that the following Kotlin-specific elements are accurately represented:
- Sealed classes: Sealed classes are displayed with their subclasses.
- Suspend functions: Suspend functions are also indicated in the UML diagram as such.
- Companion objects as static: Variables and Functions of Companion objects are identified as static elements.

### Customize the packages used for the UML Diagram

Take control of which packages should be used for your UML diagrams by setting:
- Specify used packages: Specify specific packages that should be used for the UML diagram.
- Excluded packages: Hide packages that should not be part of the UML diagram.
- Include/Exclude empty packages: Decide whether to include or exclude empty packages for the UML diagram.

***


## Configuration options
The following options can be set using ksp.
```
ksp {
    arg("excludedPackages","com.do.not.add,com.app.main")
    arg("excludedFunctions","<init>,finalize")
    arg("showPublicClasses","true")
    arg("showPublicProperties","true")
    arg("showPublicFunctions","true")
    arg("showInternalClasses","true")
    arg("showInternalProperties","true")
    arg("showInternalFunctions","true")
    arg("showPrivateClasses","true")
    arg("showPrivateProperties","true")
    arg("showPrivateFunctions","true")
    arg("showInheritance","true")
    arg("showPropertyRelations","true")
    arg("showPackages","false")
    arg("allowEmptyPackage","true")
}
```

Set `excludedPackages` to exclude packages.
Packages need to be separated by a ','. 

Set `excludedFunctions` to exclude functions.
Functions need to be separated by a ','. 

If `showPublicClasses` is set to `true` all public classes are added as class Entry for the generated UML Diagram.  
Default is `true`

If `showPublicProperties` is set to `true` all public classes are added as class Entry for the generated UML Diagram.  
Default is `true`.

If `showPublicFunctions` is set to `true` all public classes are added as class Entry for the generated UML Diagram.  
Default is `true`.

If `showInternalClasses` is set to `true` all public classes are added as class Entry for the generated UML Diagram.  
Default is `true`.

If `showInternalProperties` is set to `true` all public classes are added as class Entry for the generated UML Diagram.  
Default is `true`.

If `showInternalFunctions` is set to `true` all public classes are added as class Entry for the generated UML Diagram.  
Default is `true`.

If `showPrivateClasses` is set to `true` all public classes are added as class Entry for the generated UML Diagram.  
Default is `true`.

If `showPrivateProperties` is set to `true` all public classes are added as class Entry for the generated UML Diagram.  
Default is `true`.

If `showPrivateFunctions` is set to `true` all public classes are added as class Entry for the generated UML Diagram.  
Default is `true`.

If `showInheritance` is set to `true`, inheritance between classes is shown.
Default is `true`

If `showPropertyRelations` is set to `true`, relations between classes is shown.
Default is `true`

If `showPackages` is set to `true`, packages are visualized.
Default is `false`

If `allowEmptyPackage` is set to `true`, all classes without a package are also visualized.
Default is `true`
