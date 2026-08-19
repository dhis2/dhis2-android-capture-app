package org.dhis2.mobile.plugin.gradle

import java.io.DataInputStream
import java.io.IOException

/**
 * Extracts the class names a compiled class file refers to, by reading its constant pool.
 *
 * Every type a class mentions — supertypes, field and parameter types, method owners, `::class`
 * literals — lands in the constant pool as a `CONSTANT_Class` entry pointing at a UTF-8 name. Reading
 * just those two tag types answers "what does this class touch?" without a bytecode library, which
 * matters because this runs inside a Gradle plugin whose classpath is deliberately thin.
 *
 * The format is fixed by the JVM specification (§4.4) and has not changed in a way that affects this
 * since Java 7, so the entry sizes below are stable rather than a guess.
 */
internal object ConstantPoolReader {
    private const val MAGIC = 0xCAFEBABE.toInt()

    private const val TAG_UTF8 = 1
    private const val TAG_INTEGER = 3
    private const val TAG_FLOAT = 4
    private const val TAG_LONG = 5
    private const val TAG_DOUBLE = 6
    private const val TAG_CLASS = 7
    private const val TAG_STRING = 8
    private const val TAG_FIELD_REF = 9
    private const val TAG_METHOD_REF = 10
    private const val TAG_INTERFACE_METHOD_REF = 11
    private const val TAG_NAME_AND_TYPE = 12
    private const val TAG_METHOD_HANDLE = 15
    private const val TAG_METHOD_TYPE = 16
    private const val TAG_DYNAMIC = 17
    private const val TAG_INVOKE_DYNAMIC = 18
    private const val TAG_MODULE = 19
    private const val TAG_PACKAGE = 20

    /**
     * Class names referenced by [classBytes], in JVM internal form (`org/hisp/dhis/…`).
     *
     * Returns empty rather than throwing on anything it cannot parse: this feeds a build-time
     * warning, and a class file shape this does not recognise is not a reason to fail someone's
     * build.
     */
    fun classNames(classBytes: ByteArray): Set<String> =
        try {
            readClassNames(classBytes)
        } catch (_: IOException) {
            emptySet()
        } catch (_: IndexOutOfBoundsException) {
            emptySet()
        }

    private fun readClassNames(classBytes: ByteArray): Set<String> {
        val input = DataInputStream(classBytes.inputStream())

        if (input.readInt() != MAGIC) return emptySet()
        input.readUnsignedShort() // minor version
        input.readUnsignedShort() // major version

        val poolCount = input.readUnsignedShort()
        val utf8 = HashMap<Int, String>()
        val classNameIndices = HashSet<Int>()

        // The pool is 1-indexed, and long/double entries occupy two slots — hence the manual cursor.
        var index = 1
        while (index < poolCount) {
            when (val tag = input.readUnsignedByte()) {
                TAG_UTF8 -> utf8[index] = input.readUTF()
                TAG_CLASS -> classNameIndices += input.readUnsignedShort()
                TAG_STRING, TAG_METHOD_TYPE, TAG_MODULE, TAG_PACKAGE -> input.skipBytes(2)
                TAG_INTEGER, TAG_FLOAT -> input.skipBytes(4)
                TAG_FIELD_REF, TAG_METHOD_REF, TAG_INTERFACE_METHOD_REF,
                TAG_NAME_AND_TYPE, TAG_DYNAMIC, TAG_INVOKE_DYNAMIC,
                -> input.skipBytes(4)

                TAG_METHOD_HANDLE -> input.skipBytes(3)
                TAG_LONG, TAG_DOUBLE -> {
                    input.skipBytes(8)
                    index++ // occupies two pool slots
                }

                else -> throw IOException("Unknown constant pool tag $tag")
            }
            index++
        }

        return classNameIndices.mapNotNullTo(HashSet()) { utf8[it] }.mapTo(HashSet()) { it.normalise() }
    }

    /** Array types appear as descriptors (`[Lorg/foo/Bar;`); reduce them to the element type. */
    private fun String.normalise(): String =
        trimStart('[')
            .removePrefix("L")
            .removeSuffix(";")
}
