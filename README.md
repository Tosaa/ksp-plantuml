# PumlKSP 
**(Work in progress, unreleased!)**

PumlKSP is designed to configure and create Class Diagrams in the plantuml format, by applying a KSP Plugin on your Project.

## Try it out
Run `./gradlew :app:kspKotlin` to generate a Plantuml Classdiagram.  
The output can be found at `app/build/resources/main/ClassDiagram.puml`.  
It can be rendered by using the Intellij Plantuml Plugin or the [Plantuml.com](https://plantuml.com/)

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
    arg("showRelations","true")
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

If `showRelations` is set to `true`, relations between classes is shown.
Default is `true`

If `showPackages` is set to `true`, packages are visualized.
Default is `false`

If `allowEmptyPackage` is set to `true`, all classes without a package are also visualized.
Default is `true`
