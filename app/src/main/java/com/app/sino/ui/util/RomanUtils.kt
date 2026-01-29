package com.app.sino.ui.util

fun String.romanToDecimal(): Int {
    val romanMap = mapOf('I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100, 'D' to 500, 'M' to 1000)
    var res = 0
    val s = this.uppercase()
    var i = 0
    while (i < s.length) {
        val s1 = romanMap[s[i]] ?: return this.toIntOrNull() ?: 999
        if (i + 1 < s.length) {
            val s2 = romanMap[s[i + 1]] ?: 0
            if (s1 >= s2) {
                res += s1
                i++
            } else {
                res += s2 - s1
                i += 2
            }
        } else {
            res += s1
            i++
        }
    }
    return res
}

fun Int.toRoman(): String {
    val values = intArrayOf(10, 9, 5, 4, 1)
    val romanLiterals = arrayOf("X", "IX", "V", "IV", "I")
    val roman = StringBuilder()
    var num = this
    for (i in values.indices) {
        while (num >= values[i]) {
            num -= values[i]
            roman.append(romanLiterals[i])
        }
    }
    return roman.toString()
}
