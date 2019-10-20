package io.futz.aws.parser


const val DOUBLE_COLON = "::"
const val DOT = "."

class KeyParser {

    /**
     * Given a key, parse it and return a string array where:
     * - array[0] is package name
     * - array[1] is class name
     */
    fun parse(key: String): Array<String> {
        var packageName = ""
        var className: String
        if (key.contains(DOUBLE_COLON) && key.contains(DOT)) {
            val parts = key.split(DOUBLE_COLON)
            packageName = key.substring(0, key.lastIndexOf(DOT)).replace(
                DOUBLE_COLON,
                DOT
            ).toLowerCase()
            className = parts[parts.size - 1]
            if (className.contains(DOT)) {
                className = className.substring(className.lastIndexOf(DOT) + 1)
            }
        } else if (key.contains(DOUBLE_COLON)) {

            packageName = key.substring(0, key.lastIndexOf(DOUBLE_COLON)).replace(
                DOUBLE_COLON,
                DOT
            ).toLowerCase()
            className = key.substring(key.lastIndexOf(DOUBLE_COLON) + 2, key.length)

        } else {
            className = key
        }

        // special case
        if (className == "Tag") {
            packageName = "aws"
        }

        return arrayOf(packageName, className)
    }
}