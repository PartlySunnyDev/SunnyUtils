package me.partlysunny.util.reflection

/**
 * NOT BY ME!!! [https://gist.github.com/Lauriichan/294c64b63067dcb6a9a8658f2d040256](https://gist.github.com/Lauriichan/294c64b63067dcb6a9a8658f2d040256)
 */
class JavaTracker private constructor() {
    init {
        throw UnsupportedOperationException("Utility class")
    }

    companion object {
        private val stack: Array<StackTraceElement>
            private get() = Throwable().stackTrace

        fun getClassFromStack(offset: Int): Class<*>? {
            val element = stack[3 + offset]
                ?: return null
            return JavaAccessor.getClass(element.className)
        }

        val callerClass: Class<*>?
            get() = getClassFromStack(1)
    }
}
