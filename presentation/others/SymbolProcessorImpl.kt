class MyProcessor : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val files = resolver.getAllFiles()
        files.forEach { file ->
            // myVisitor will analyse every component on File-level
            val myVisitor = KSVisitorVoid()
            file.accept(myVisitor, Unit)
        }
        return files.toList()
    }
}