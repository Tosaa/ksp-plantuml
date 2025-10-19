# Kotlin Symbol Processing in Action: How KSP and Plantuml keeps your documentation up to date

## Abstract

It can be hard to write and maintain documentation. It can be outdated quickly and often there is just limited time to
update the documentation. This can be inconvenient, especially for documentation and manuals that are given to 3rd
parties.
The Kotlin Plantuml Generator library should decrease the overhead to draw diagrams of a codebase by generating Plantuml
diagrams based on the Kotlin sourcecode and Kotlin Symbol Processing. Since it can be integrated as a gradle task, the documentation can stay up to
date with every build. By this it's a great tool for anyone who develops Kotlin-libraries and shares them with others.
The library handles a lot of class dependencies, inheritance, type resolutions, and interpreting Kotlin-specific features.

During its implementation, I encountered a few challenges in the Kotlin Symbol Processing world.
And in this talk, I will address these challenges, share my experiences, and inspire the audience to use KSP on their own.

For example, it was difficult to display Inner Classes or Companion Objects, since it's not straightforward to access Inner Classes.
Inner Classes, Interfaces or Companion Objects will not be visited by the `KSVisitor.visitClassDeclaration` function.
Instead, they must be accessed through the declarations of the owning class.

Another challenge was to hide inherited functions and properties. By default, a `KSClassDeclaration` offers access to all functions and properties that are accessible on this class.
The task was to figure out if a function has been inherited or not.
Join this talk to find out why the override modifier won't be enough and how you still can find out if a function or property has been inherited.

A few more takeaways will be:
- How Inheritance of classes can be identified
- How to determine types for function return values and variables, taking into account lambdas, generics, collections, simple classes, and primitive types
- How to identify extension functions and to resolve their receiver
- How to deal with Sealed classes and Data classes

**Tags:** Kotlin Symbol Processing, Meta-Programming, Documentation, Code generation, Plantuml  
**Level:** Medium, Kotlin Symbol Processing will not be explained from scratch (if not required), basic knowledge is recommended. No
Plantuml knowledge required.  
**Category:** meta-programming  
**Session length:** 45 min

## Additional notes
About me:
- Living near Munich
- Android dev with domain in the Automotive industry
- 4th time attending on the Kotlin Conf
- Speaker in company internal presentations and events
- Github: https://github.com/Tosaa/ksp-plantuml

## Topic: Inheritance
### Story:
For the Plantuml Diagrams I wanted to show which properties belong to the Parent and which were added newly to the class.  
In the diagram a property or function that is inherited by a parent, should not be shown again on the childs class representation.  

By implementing this, I discovered, that it's not that easy to figure out which properties and functions are inherited.  
I was happy to find time to play around, and find a few interesting specialities.

1. Everything inherits from `Any`, but that's an inheritance I did not want to show.
2. Enums inherit from `Enum`, but that's an inheritance I did not want to show.
3. If you override, the function/property qualifier changes. If you inherit the abstract parent implementation, it won't.

### Challenge 1: Everything inherits from Any
If you iterate over a `KSClassDeclaration.superTypes` you should be aware, that it will never be empty.  
And if you are not interested in `kotlin.Any`, you must find a way to skip that parent.  

Side note:  
Declaration of `kotlin.Any` provides functions like `equals`, `hashCode`, `toString`.

### Challenge 2: Enums inherit Enum
If you iterate over a `KSClassDeclaration.superTypes` of an `Enum` you should be aware, that it will never be empty.  
And if you are not interested in `kotlin.Enum`, you must find a way to skip that parent.

Side note:  
- The resolved `Enum<T>` is a class that hold your defined Enum as generic.
- - Calling `arguments` on the `KSTypeImpl` of `Enum<T>` will return the class that you defined as Enum.
- - Calling `typeParameters` on the `KSTypeImpl.declaration` of `Enum<T>` will return a generic reference to `E`. 
- Declaration of `kotlin.Enum` inherits from `Comparable<E>` and `Serializable`
- Declaration of `kotlin.Enum` provides properties like `name`, `ordinal`.


### Challenge 3: How to check if a function or property is inherited or newly added
If you call `KSClassDeclaration.allFunctions()` and `KSClassDeclaration.allProperties()` you retrieve a mix of functions that are declared or implemented on the parent and that were newly added.

If you make use of abstract classes or open properties and open functions, you can override these, but you don't have to.  
If you make use of Interfaces you have to override unless your child is an Interface too.  
If you override, the function or property is marked as override by the list of modifiers.  
If you don't override, the function or property will not be marked as override.  

Simply checking the override modifier will not be enough to figure out if a function or property was implemented by the parent or by the class being considered.
For each property and function, it must be decided if it is declared by the parent or newly added by the child.
If the override modifier is not present, the qualified name must be compared with the qualified name of the class being considered, to figure out that this function is implemented by a parent.

Here is a small example:  
**Parent** with abstract implementation of `run`:
```
class qualified name: explorer.database.AbstractAnimal
function qualified name: explorer.database.AbstractAnimal.run
modifiers: []
```

> The qualified name of the class and the function before the simple function name, do match.  
> The function is not marked as override.  
> The function is added by this class.

**Child** without override of `run`:
```
class qualified name: explorer.database.pets.Dog
function qualified name: explorer.database.AbstractAnimal.run
modifiers: []
```

> The qualified name of the class and the function before the simple function name, do not match.  
> The function is inherited.

**Child** with override of `run`:
```
class qualified name: explorer.database.pets.Cat
function qualified name: explorer.database.pets.Cat.run
modifiers: [OVERRIDE]
```

> The qualified name of the class and the function before the simple function name, do match.  
> The function is marked as override.  
> The function is inherited.

Eventually we can hide the function in the Plantuml diagram for the child classes, since we figured out, that the function is shown in a parents class already.

## Topic: Determining Variable and Function return types
_Todo: Gather notes_ 

## Topic: Extension functions and their receivers
_Todo: Gather notes_

## Topic: Specialities with Sealed classes and Data classes.
_Todo: Gather notes_
