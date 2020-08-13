package org.dhis2.utils.extension

/**
 * Invokes method from a object using reflection
 */
fun <T : Any> T.invoke(method: String): Any {
    return javaClass.invoke(this, method);
}

/**
 * invokes a class method by looking for it in its base classes if not exist
 */
private fun <T> Class<T>.invoke(obj: T, method: String): Any {
    return try {
        this.getMethod(method).invoke(obj)
    } catch (e: IllegalAccessException) {
        if (this.superclass != null) {
            superclass.invoke(obj, method)
        } else {
            throw e
        }
    }
}