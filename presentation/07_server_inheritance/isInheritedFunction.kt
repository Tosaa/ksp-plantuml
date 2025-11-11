fun KSFunctionDeclaration.isInherited(): Boolean {
    if (this.modifiers.contains(Modifier.OVERRIDE)) {
        return true
    }
    // ... more checks
    return true
}

fun KSPropertyDeclaration.isInherited(): Boolean {
    if (this.modifiers.contains(Modifier.OVERRIDE)) {
        return true
    }
    // ... more checks
    return true
}