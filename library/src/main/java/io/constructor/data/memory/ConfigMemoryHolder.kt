package io.constructor.data.memory

import io.constructor.util.base64Decode
import io.constructor.util.base64Encode
import javax.inject.Inject

class ConfigMemoryHolder @Inject constructor() {

    private var backingString = ""

    var testCellParams: List<Pair<String, String>?>
        get() {
            val result = mutableListOf<Pair<String, String>>()
            val s = backingString
            if (s.isNotEmpty()) {
                s.split(";").forEach {
                    val pair = it.split("=")
                    result.add("ef-${pair[0].base64Decode()}" to pair[1].base64Decode())
                }
            }
            return result
        }
        set(value) {
            var combined = ""
            value.filter { it != null }.forEachIndexed { index, pair ->
                combined += if (index == 0) {
                    "${pair!!.first.base64Encode()}=${pair.second.base64Encode()}"
                } else {
                    ";${pair!!.first.base64Encode()}=${pair.second.base64Encode()}"
                }
            }
            backingString = combined
        }

    var autocompleteResultCount: Map<String, Int>? = null
}