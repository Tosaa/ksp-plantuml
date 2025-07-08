---
name: Bug report
about: Report wrong or unexpected visual representation
title: "[Bug] "
labels: ''
assignees: Tosaa

---

### Description: 
Please describe briefly what happens when you use the plugin. What is the problem you are experiencing?

**Bug Type** :beetle:
- [ ] Bug in Visual representation: Generated PUML is different than expected
- [ ] Bug in generating PUML: Generated PUML cannot be created
- [ ] Bug in setup: library has issues when setting it up
- [ ] Something else 

## Example:
### Code
Please add an example code that reproduces the issue. This should be a minimal code that demonstrates the problem.

```
package abc
class Foo(){

}
```

### Expected Result
Please describe what you expect the plugin to do to solve the problem. What should the plugin output or behave like?

```
@startuml
class "Foo" as abc_foo
@enduml
```

### Actual Result
Please describe what the library actually does when you use the example code. What is the actual output or behavior?

### Error Description
Please add a detailed description of the error. What exactly happens when you use the plugin?

### Configuration
Please add the version of the library you are using.
Please also add the KSP parameters that were used.

Thank you for your help in fixing this bug!
