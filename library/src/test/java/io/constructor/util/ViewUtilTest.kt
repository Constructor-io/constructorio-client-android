package io.constructor.util

import android.graphics.Typeface
import android.text.Spannable
import android.text.style.StyleSpan
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class ViewUtilTest {

    private val stylingFunction = { spannable: Spannable, start: Int, end: Int ->
        spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, 0)
    }

    @Before
    fun setUp() {
    }

    @Test
    fun reverseHighlightOneMatch() {
        val spannable = ViewUtil.reverseHighlight("frozen corn", listOf("corn"), stylingFunction)
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        assertEquals(1, spans.size)
        assertEquals(0, spannable.getSpanStart(spans[0]))
        assertEquals(7, spannable.getSpanEnd(spans[0]))
    }

    @Test
    fun reverseHighlightNoMatch() {
        val spannable = ViewUtil.reverseHighlight("frozen corn", listOf("dog"), stylingFunction)
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        assertEquals(0, spans.size)
    }

    @Test
    fun reverseHighlightTwoMatches() {
        val spannable = ViewUtil.reverseHighlight("beyond dog food", listOf("dog","food"), stylingFunction)
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        assertEquals(1, spans.size)
        assertEquals(0, spannable.getSpanStart(spans[0]))
        assertEquals(7, spannable.getSpanEnd(spans[0]))

    }

    @Test
    fun reverseHighlightOneMatchTwoHighlights() {
        val spannable = ViewUtil.reverseHighlight("hot dog buns", listOf("dog"), stylingFunction)
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        assertEquals(2, spans.size)
        assertEquals(0, spannable.getSpanStart(spans[0]))
        assertEquals(4, spannable.getSpanEnd(spans[0]))
        assertEquals(7, spannable.getSpanStart(spans[1]))
        assertEquals(12, spannable.getSpanEnd(spans[1]))

    }

}