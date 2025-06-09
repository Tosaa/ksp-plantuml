fun generateEnum(packageName: String, className: String, entries:List<String>, properties: List<String>, functions: List<String>, innerClassScope: String = ""): String {
    val optimizedFunctions = functions.map {
        if (!it.contains("{.*}")) {
            """
        $it {
            // TO DO: implement function logic
            return Any() as ${it.split(" ").last()}!!
        }"""
        } else {
            it
        }
    }
    return """
${packageName.takeIf { it.isNotBlank() }?.let { "        package $packageName\n" } ?: ""}        
        
    enum class $className {
${entries.joinToString(separator = ",\n", postfix = ";") { "\t\t$it" }}
        
${properties.joinToString(separator = "\n") { "\t\t$it" }}  
  
${optimizedFunctions.joinToString(separator = "\n") { "\t\t$it" }}    

${innerClassScope.lines().joinToString("\n") { "\t\t$it" }}
    }
    """
}

fun generateInterface(packageName: String, className: String, properties: List<String>, functions: List<String>, innerClassScope: String = ""): String {
    // Values in interfaces cannot be assigned
    val optimizedProperties = properties.map { it.split("=").first() }
    // Functions in interfaces cannot be implemented
    val optimizedFunctions = functions.map { it.split("{").first() }
    return generateCode(
        "interface",
        packageName,
        className,
        optimizedProperties,
        optimizedFunctions,
        innerClassScope
    )
}

fun generateObject(packageName: String, className: String, properties: List<String>, functions: List<String>, innerClassScope: String = ""): String {
    val optimizedFunctions = functions.map {
        if (!it.contains("{.*}")) {
            """
        $it {
            // TO DO: implement function logic
            return Any() as ${it.split(" ").last()}!!
        }"""
        } else {
            it
        }
    }
    return generateCode(
        "object",
        packageName,
        className,
        properties,
        optimizedFunctions,
        innerClassScope
    )
}

fun generateClass(packageName: String, className: String, properties: List<String>, functions: List<String>, innerClassScope: String = ""): String {
    val optimizedFunctions = functions.map {
        if (!it.contains("{.*}")) {
            """
        $it {
            // TO DO: implement function logic
            return Any() as ${it.split(" ").last()}!!
        }"""
        } else {
            it
        }
    }
    return generateCode(
        "class",
        packageName,
        className,
        properties,
        optimizedFunctions,
        innerClassScope
    )
}

private fun generateCode(type: String, packageName: String, className: String, properties: List<String>, functions: List<String>, innerClassScope: String = ""): String {
    return """
${packageName.takeIf { it.isNotBlank() }?.let { "        package $packageName\n" } ?: ""}        
        
    $type $className {    
${properties.joinToString(separator = "\n") { "\t\t$it" }}  
  
${functions.joinToString(separator = "\n") { "\t\t$it" }}    

${innerClassScope.lines().joinToString("\n") { "\t\t$it" }}
    }
    """
}