override fun visitClassDeclaration(
    classDeclaration: KSClassDeclaration,
    data: Unit
) {
    classDeclaration.declarations
        .filterIsInstance<KSClassDeclaration>()
        .forEach { innerClass ->
            val kind = innerClass.classKind
            when (kind) {
                ClassKind.INTERFACE -> analyseInterface(innerClass)
                ClassKind.CLASS -> analyseClass(innerClass)
                ClassKind.ENUM_CLASS -> analyseEnum(innerClass)
                ClassKind.ENUM_ENTRY -> analyseEnumEntry(innerClass)
                ClassKind.OBJECT -> if (innerClass.isCompanionObject) {
                    analyseCompanionObject(innerClass)
                } else {
                    analyseObject(innerClass)
                }

                ClassKind.ANNOTATION_CLASS -> analyseAnnotation(innerClass)
            }
        }

    analyseInnerClasses()
}