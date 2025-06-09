package second

import first.OuterClass

sealed class ABC {
    abstract val tuc: Int

    data object A : ABC() {
        override val tuc: Int
            get() = 6
    }

    class B : ABC() {
        override val tuc: Int
            get() = 2
    }

    sealed class C(val note: String) : ABC() {
        override val tuc: Int
            get() = note.hashCode()

        class D(val innerClass: OuterClass.InnerClass) : C(innerClass.bar.toString() + "D")
        class E(note: String) : C(note + "E")
    }
}