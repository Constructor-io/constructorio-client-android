package io.constructor.util

import android.text.Spannable
import android.text.SpannableStringBuilder

/**
 * @suppress
 */
object ViewUtil {

    fun reverseHighlight(text: String, matchedTerms: List<String>?, style: ((span: Spannable, spanStart: Int, spanEnd: Int) -> Unit)? = null): Spannable {
        return SpannableStringBuilder(text).apply {
            val spans = arrayListOf<Pair<Int, Int>>()
            val spansToUse = arrayListOf<Pair<Int, Int>>()
            matchedTerms?.let {
                for (t in it) {
                    val start = indexOf(t)
                    if (start != -1) {
                        spans += Pair(start, start + t.length)
                    }
                }
                var lastEnd = 0
                var firstRun = true
                spans.sortBy { it.first }
                spans.forEach {
                    if (firstRun) {
                        spansToUse += Pair(0, it.first)
                        firstRun = false
                    }
                    if (lastEnd != 0) {
                        spansToUse += Pair(lastEnd, it.first)
                    }
                    lastEnd = it.second
                }
                if (spansToUse.isNotEmpty()) {
                    spansToUse += Pair(lastEnd, length)
                }
                spansToUse.filter { it.first != it.second && this.subSequence(it.first, it.second).isNotBlank() }.forEach { style?.invoke(this, it.first, it.second) }
            }
        }
    }

}
