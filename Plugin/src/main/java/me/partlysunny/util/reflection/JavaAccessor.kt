package me.partlysunny.util.reflection

import sun.misc.Unsafe
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles.Lookup
import java.lang.invoke.VarHandle
import java.lang.reflect.*
import java.util.*

/*
 * NOT BY ME!!! https://gist.github.com/Lauriichan/294c64b63067dcb6a9a8658f2d040256
 * */
class JavaAccessor private constructor() {
    private val unsuccessful: AccessUnsuccessful = AccessUnsuccessful
    private var unsafe: Unsafe? = null
    private var lookup: Lookup? = null

    init {
        val option: Class<*>? = JavaTracker.callerClass
        if (option == null || option != JavaAccessor::class.java) {
            throw UnsupportedOperationException("Utility class")
        }
    }

    /*
     * Static Accessors
     */
    fun unsafe(): Unsafe? {
        return if (unsafe != null) {
            unsafe
        } else try {
            val field = Unsafe::class.java.getDeclaredField("theUnsafe")
            field.isAccessible = true
            field[null] as Unsafe
        } catch (exp: Exception) {
            null
        }
    }

    fun lookup(): Lookup {
        return lookup
            ?: (getStatWithBonusicValueUnsafe(getField(Lookup::class.java, "IMPL_LOOKUP")) as Lookup)
    }

    fun execute(instance: Any?, method: Method?, vararg arguments: Any?): Any? {
        return if (method == null || method.parameterCount != arguments.size) {
            null
        } else try {
            if (!Modifier.isStatic(method.modifiers)) {
                if (instance == null) {
                    return null
                }
                if (arguments.size == 0) {
                    return lookup().unreflect(method).invokeWithArguments(instance)
                }
                val input = arrayOfNulls<Any>(arguments.size + 1)
                input[0] = instance
                System.arraycopy(arguments, 0, input, 1, arguments.size)
                return lookup().unreflect(method).invokeWithArguments(*input)
            }
            lookup().unreflect(method).invokeWithArguments(*arguments)
        } catch (e: Throwable) {
            null
        }
    }

    fun init(constructor: Constructor<*>?, vararg arguments: Any?): Any? {
        return if (constructor == null || constructor.parameterCount != arguments.size) {
            null
        } else try {
            lookup().unreflectConstructor(constructor).invokeWithArguments(*arguments)
        } catch (e: Throwable) {
            null
        }
    }

    fun handle(field: Field?, force: Boolean): VarHandle? {
        if (field == null) {
            return null
        }
        if (force) {
            unfinalize(field)
        }
        return try {
            lookup().unreflectVarHandle(field)
        } catch (e: Throwable) {
            null
        }
    }

    fun handleGetter(field: Field?): MethodHandle? {
        return if (field == null) {
            null
        } else try {
            lookup().unreflectGetter(field)
        } catch (e: Throwable) {
            null
        }
    }

    /*
     * Static Utilities
     */
    fun handleSetter(field: Field?): MethodHandle? {
        if (field == null) {
            return null
        }
        unfinalize(field)
        return try {
            lookup().unreflectSetter(field)
        } catch (e: Throwable) {
            null
        }
    }

    fun handle(method: Method?): MethodHandle? {
        return if (method == null) {
            null
        } else try {
            lookup().unreflect(method)
        } catch (e: Throwable) {
            null
        }
    }

    fun handle(constructor: Constructor<*>?): MethodHandle? {
        return if (constructor == null) {
            null
        } else try {
            lookup().unreflectConstructor(constructor)
        } catch (e: Throwable) {
            null
        }
    }

    fun executeSafe(instance: Any?, handle: MethodHandle?, vararg arguments: Any?): Any? {
        return if (handle == null || handle.type().parameterCount() != arguments.size) {
            null
        } else try {
            if (instance != null) {
                if (arguments.size == 0) {
                    return handle.invokeWithArguments(instance)
                }
                val input = arrayOfNulls<Any>(arguments.size + 1)
                input[0] = instance
                System.arraycopy(arguments, 0, input, 1, arguments.size)
                return handle.invokeWithArguments(*input)
            }
            handle.invokeWithArguments(*arguments)
        } catch (e: Throwable) {
            null
        }
    }

    fun getValueSafe(instance: Any?, handle: VarHandle?): Any? {
        return if (handle == null) {
            null
        } else try {
            if (instance == null) {
                handle.getVolatile()
            } else handle.getVolatile(instance)
        } catch (e: Throwable) {
            throw unsuccessful
        }
    }

    fun setValueSafe(instance: Any?, handle: VarHandle?, value: Any?) {
        if (handle == null || value != null && !handle.varType().isAssignableFrom(value.javaClass)) {
            return
        }
        try {
            if (instance != null) {
                handle.setVolatile(value)
                return
            }
            handle.setVolatile(instance, value)
        } catch (e: Throwable) {
            throw unsuccessful
        }
    }

    fun getObjectValueSafe(instance: Any?, field: Field?): Any? {
        return if (instance == null || field == null) {
            null
        } else try {
            lookup().unreflectGetter(field).invoke(instance)
        } catch (e: Throwable) {
            throw unsuccessful
        }
    }

    fun getStatWithBonusicValueSafe(field: Field?): Any? {
        return if (field == null) {
            null
        } else try {
            lookup().unreflectGetter(field).invoke()
        } catch (e: Throwable) {
            throw unsuccessful
        }
    }

    fun setObjectValueSafe(instance: Any?, field: Field?, value: Any?) {
        if (instance == null || field == null || value != null && !field.type.isAssignableFrom(value.javaClass)) {
            return
        }
        unfinalize(field)
        try {
            lookup().unreflectSetter(field).invokeWithArguments(instance, value)
        } catch (e: Throwable) {
            throw unsuccessful
        }
    }

    fun setStaticValueSafe(field: Field?, value: Any?) {
        if (field == null || value != null && !field.type.isAssignableFrom(value.javaClass)) {
            return
        }
        unfinalize(field)
        try {
            lookup().unreflectSetter(field).invokeWithArguments(value)
        } catch (e: Throwable) {
            throw unsuccessful
        }
    }

    fun getObjectValueUnsafe(instance: Any?, field: Field?): Any? {
        if (instance == null || field == null) {
            return null
        }
        val unsafe = unsafe()
        return unsafe!!.getObjectVolatile(instance, unsafe.objectFieldOffset(field))
    }

    fun getStatWithBonusicValueUnsafe(field: Field?): Any? {
        if (field == null) {
            return null
        }
        val unsafe = unsafe()
        return unsafe!!.getObjectVolatile(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field))
    }

    fun setObjectValueUnsafe(instance: Any?, field: Field?, value: Any?) {
        if (instance == null || field == null) {
            return
        }
        unfinalize(field)
        val unsafe = unsafe()
        if (value == null) {
            unsafe!!.putObject(instance, unsafe.objectFieldOffset(field), null)
            return
        }
        if (field.type.isAssignableFrom(value.javaClass)) {
            unsafe!!.putObject(instance, unsafe.objectFieldOffset(field), field.type.cast(value))
        }
    }

    fun setStaticValueUnsafe(field: Field?, value: Any?) {
        if (field == null) {
            return
        }
        unfinalize(field)
        val unsafe = unsafe()
        if (value == null) {
            unsafe!!.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), null)
            return
        }
        if (field.type.isAssignableFrom(value.javaClass)) {
            unsafe!!.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), field.type.cast(value))
        }
    }

    private fun unfinalize(field: Field) {
        if (!Modifier.isFinal(field.modifiers)) {
            return
        }
        try {
            lookup().findSetter(Field::class.java, "modifiers", Int::class.javaPrimitiveType)
                .invokeExact(field, field.modifiers and Modifier.FINAL.inv())
        } catch (e: Throwable) {
            // Ignore
        }
    }

    /*
     * Internal Exceptions
     */
    private object AccessUnsuccessful : RuntimeException() {
        private const val serialVersionUID = 1L
    }

    companion object {
        private val INSTANCE = JavaAccessor()
        fun getStatWithBonusicValue(handle: VarHandle?): Any? {
            return INSTANCE.getValueSafe(null, handle)
        }

        fun getValue(instance: Any?, handle: VarHandle?): Any? {
            return INSTANCE.getValueSafe(instance, handle)
        }

        /*
     * Method invokation
     */
        fun setStaticValue(handle: VarHandle?, value: Any?) {
            INSTANCE.setValueSafe(null, handle, value)
        }

        fun setValue(instance: Any?, handle: VarHandle?, value: Any?) {
            INSTANCE.setValueSafe(instance, handle, value)
        }

        /*
     * Safe Accessors
     */
        fun invokeStatic(handle: MethodHandle?, vararg arguments: Any?): Any? {
            return INSTANCE.executeSafe(null, handle, *arguments)
        }

        operator fun invoke(instance: Any?, handle: MethodHandle?, vararg arguments: Any?): Any? {
            return INSTANCE.executeSafe(instance, handle, *arguments)
        }

        fun instance(clazz: Class<*>?): Any? {
            return INSTANCE.init(getConstructor(clazz))
        }

        fun instance(constructor: Constructor<*>?, vararg arguments: Any?): Any? {
            return INSTANCE.init(constructor, *arguments)
        }

        fun invokeStatic(method: Method?, vararg arguments: Any?): Any? {
            return INSTANCE.execute(null, method, *arguments)
        }

        /*
     * Safe Accessors helper
     */
        operator fun invoke(instance: Any?, method: Method?, vararg arguments: Any?): Any? {
            return INSTANCE.execute(instance, method, *arguments)
        }

        fun setValue(instance: Any?, clazz: Class<*>?, fieldName: String?, value: Any?) {
            setValue(instance, getField(clazz, fieldName), value)
        }

        fun setObjectValue(instance: Any?, clazz: Class<*>?, fieldName: String?, value: Any?) {
            setObjectValue(instance, getField(clazz, fieldName), value)
        }

        /*
     * Safe Field Modifier
     */
        fun setStaticValue(clazz: Class<*>?, fieldName: String?, value: Any?) {
            setStaticValue(getField(clazz, fieldName), value)
        }

        fun setValue(instance: Any?, field: Field?, value: Any?) {
            if (field == null) {
                return
            }
            if (Modifier.isStatic(field.modifiers)) {
                setStaticValue(field, value)
                return
            }
            setObjectValue(instance, field, value)
        }

        fun setObjectValue(instance: Any?, field: Field?, value: Any?) {
            if (instance == null || field == null) {
                return
            }
            try {
                INSTANCE.setObjectValueSafe(instance, field, value)
            } catch (unsafe: AccessUnsuccessful) {
                INSTANCE.setObjectValueUnsafe(instance, field, value)
            }
        }

        fun setStaticValue(field: Field?, value: Any?) {
            if (field == null) {
                return
            }
            try {
                INSTANCE.setStaticValueSafe(field, value)
            } catch (unsafe: AccessUnsuccessful) {
                INSTANCE.setStaticValueUnsafe(field, value)
            }
        }

        /*
     * Unsafe Field Modifier
     */
        fun getValue(instance: Any?, clazz: Class<*>?, fieldName: String?): Any? {
            return getValue(instance, getField(clazz, fieldName))
        }

        fun getObjectValue(instance: Any?, clazz: Class<*>?, fieldName: String?): Any? {
            return getObjectValue(instance, getField(clazz, fieldName))
        }

        fun getStatWithBonusicValue(clazz: Class<*>?, fieldName: String?): Any? {
            return getStatWithBonusicValue(getField(clazz, fieldName))
        }

        fun getValue(instance: Any?, field: Field?): Any? {
            if (field == null) {
                return null
            }
            return if (Modifier.isStatic(field.modifiers)) {
                getStatWithBonusicValue(field)
            } else getObjectValue(instance, field)
        }

        /*
     * Internal Utilities
     */
        fun getObjectValue(instance: Any?, field: Field?): Any? {
            return if (instance == null || field == null) {
                null
            } else try {
                INSTANCE.getObjectValueSafe(instance, field)
            } catch (unsafe: AccessUnsuccessful) {
                INSTANCE.getObjectValueUnsafe(instance, field)
            }
        }

        /*
     * Static Accessors Helper
     */
        fun getStatWithBonusicValue(field: Field?): Any? {
            return if (field == null) {
                null
            } else try {
                INSTANCE.getStatWithBonusicValueSafe(field)
            } catch (unsafe: AccessUnsuccessful) {
                INSTANCE.getStatWithBonusicValueUnsafe(field)
            }
        }

        fun accessField(field: Field?): VarHandle? {
            return INSTANCE.handle(field, false)
        }

        fun accessField(field: Field?, forceModification: Boolean): VarHandle? {
            return INSTANCE.handle(field, forceModification)
        }

        fun accessFieldGetter(field: Field?): MethodHandle? {
            return INSTANCE.handleGetter(field)
        }

        fun accessFieldSetter(field: Field?): MethodHandle? {
            return INSTANCE.handleSetter(field)
        }

        fun accessMethod(method: Method?): MethodHandle? {
            return INSTANCE.handle(method)
        }

        /*
     * Static Implementation
     */
        // Invokation
        fun accessConstructor(constructor: Constructor<*>?): MethodHandle? {
            return INSTANCE.handle(constructor)
        }

        fun getField(clazz: Class<*>?, field: String?): Field? {
            return if (clazz == null || field == null) {
                null
            } else try {
                clazz.getDeclaredField(field)
            } catch (ignore: NoSuchFieldException) {
                try {
                    clazz.getField(field)
                } catch (ignore0: NoSuchFieldException) {
                    null
                } catch (ignore0: SecurityException) {
                    null
                }
            } catch (ignore: SecurityException) {
                try {
                    clazz.getField(field)
                } catch (ignore0: NoSuchFieldException) {
                    null
                } catch (ignore0: SecurityException) {
                    null
                }
            }
        }

        fun getFieldOfType(clazz: Class<*>, type: Class<*>): Field? {
            return getFieldOfType(clazz, type, 0)
        }

        fun getFieldOfType(clazz: Class<*>, type: Class<*>, index: Int): Field? {
            val field0 = clazz.fields
            val field1 = clazz.declaredFields
            val fields = ArrayList<Field>()
            for (field in field0) {
                if (field.type != type || fields.contains(field)) {
                    continue
                }
                fields.add(field)
            }
            for (field in field1) {
                if (field.type != type || fields.contains(field)) {
                    continue
                }
                fields.add(field)
            }
            return if (fields.isEmpty() || index >= fields.size) {
                null
            } else fields[index]
        }

        // Setter
        fun getFields(clazz: Class<*>): Array<Field> {
            val field0 = clazz.fields
            val field1 = clazz.declaredFields
            val fields = HashSet<Field>()
            Collections.addAll(fields, *field0)
            Collections.addAll(fields, *field1)
            return fields.toTypedArray()
        }

        fun getFieldsOfType(clazz: Class<*>, type: Class<*>): Array<Field> {
            val field0 = clazz.fields
            val field1 = clazz.declaredFields
            val fields = HashSet<Field>()
            for (field in field0) {
                if (field.type != type) {
                    continue
                }
                fields.add(field)
            }
            for (field in field1) {
                if (field.type != type) {
                    continue
                }
                fields.add(field)
            }
            return fields.toTypedArray()
        }

        fun getMethod(clazz: Class<*>?, method: String?, vararg arguments: Class<*>?): Method? {
            return if (clazz == null || method == null) {
                null
            } else try {
                clazz.getDeclaredMethod(method, *arguments)
            } catch (ignore: NoSuchMethodException) {
                try {
                    clazz.getMethod(method, *arguments)
                } catch (ignore0: NoSuchMethodException) {
                    null
                } catch (ignore0: SecurityException) {
                    null
                }
            } catch (ignore: SecurityException) {
                try {
                    clazz.getMethod(method, *arguments)
                } catch (ignore0: NoSuchMethodException) {
                    null
                } catch (ignore0: SecurityException) {
                    null
                }
            }
        }

        fun getMethods(clazz: Class<*>): Array<Method> {
            val method0 = clazz.methods
            val method1 = clazz.declaredMethods
            val methods = HashSet<Method>()
            Collections.addAll(methods, *method0)
            Collections.addAll(methods, *method1)
            return methods.toTypedArray()
        }

        fun getConstructor(clazz: Class<*>?, vararg arguments: Class<*>?): Constructor<*>? {
            return if (clazz == null) {
                null
            } else try {
                clazz.getDeclaredConstructor(*arguments)
            } catch (ignore: NoSuchMethodException) {
                try {
                    clazz.getConstructor(*arguments)
                } catch (ignore0: NoSuchMethodException) {
                    null
                } catch (ignore0: SecurityException) {
                    null
                }
            } catch (ignore: SecurityException) {
                try {
                    clazz.getConstructor(*arguments)
                } catch (ignore0: NoSuchMethodException) {
                    null
                } catch (ignore0: SecurityException) {
                    null
                }
            }
        }

        fun getConstructors(clazz: Class<*>): Array<Constructor<*>> {
            val constructor0 = clazz.constructors
            val constructor1 = clazz.declaredConstructors
            val constructors = HashSet<Constructor<*>>()
            Collections.addAll(constructors, *constructor0)
            Collections.addAll(constructors, *constructor1)
            return constructors.toTypedArray()
        }

        // Getter
        fun getClass(name: String?): Class<*>? {
            return try {
                Class.forName(name)
            } catch (e: ClassNotFoundException) {
                null
            } catch (e: LinkageError) {
                null
            }
        }

        fun getClass(clazz: Class<*>?, name: String?): Class<*>? {
            if (clazz == null || name == null) {
                return null
            }
            val size = clazz.classes.size + clazz.declaredClasses.size
            if (size == 0) {
                return null
            }
            val classes: Array<Class<*>?> = arrayOfNulls(size)
            val tmp = clazz.classes
            System.arraycopy(tmp, 0, classes, 0, tmp.size)
            System.arraycopy(clazz.declaredClasses, tmp.size, classes, tmp.size, size - tmp.size)
            for (i in 0 until size) {
                var target = classes[i]!!.simpleName
                if (target.contains(".")) {
                    target = target.split(".".toRegex(), limit = 2).toTypedArray()[0]
                }
                if (target == name) {
                    return classes[i]
                }
            }
            return null
        }

        fun getClassFromField(clazz: Class<*>?, declared: Boolean, vararg blacklistArray: Class<*>): Class<*>? {
            if (clazz == null) {
                return null
            }
            val blacklist: Array<out Class<*>> = blacklistArray
            val fields = getFields(clazz)
            for (field in fields) {
                if (Modifier.isStatic(field.modifiers) && declared) {
                    continue
                }
                var passed = true
                for (forbidden in blacklist) {
                    if (forbidden.isAssignableFrom(field.type)) {
                        passed = false
                        break
                    }
                }
                if (!passed) {
                    continue
                }
                return field.type
            }
            return null
        }

        fun <A : Annotation?> getAnnotation(element: AnnotatedElement, annotationType: Class<A>?): A {
            val annotation = annotationType?.let { element.getAnnotation(it) }
            return annotation ?: element.getDeclaredAnnotation(annotationType)
        }

        fun <A : Annotation?> getAnnotations(element: AnnotatedElement, annotationType: Class<A>?): Array<A> {
            val annotation0 = element.getAnnotationsByType(annotationType)
            val annotation1 = element.getDeclaredAnnotationsByType(annotationType)
            if (annotation0.isNotEmpty() && annotation1.isNotEmpty()) {
                val annotations = HashSet<A>()
                Collections.addAll(annotations, *annotation0)
                Collections.addAll(annotations, *annotation1)
                return annotations.toArray(
                    java.lang.reflect.Array.newInstance(
                        annotationType,
                        annotations.size
                    ) as Array<A>
                )
            }
            return if (annotation0.isEmpty()) {
                annotation1
            } else annotation0
        }

        fun <A : Annotation> getOptionalAnnotation(element: AnnotatedElement, annotationType: Class<A>?): Optional<A> {
            return Optional.ofNullable(getAnnotation(element, annotationType))
        }
    }
}
