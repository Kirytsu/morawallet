package com.example.morawallet

import com.example.morawallet.core.util.HtmlText
import org.junit.Assert.assertEquals
import org.junit.Test

class HtmlTextTest {

    @Test
    fun strips_html_tags() {
        assertEquals("Hello world", HtmlText.clean("<p>Hello <b>world</b></p>"))
    }

    @Test
    fun removes_newsapi_truncation_marker() {
        assertEquals(
            "Markets rallied today",
            HtmlText.clean("Markets rallied today [+1234 chars]"),
        )
    }

    @Test
    fun decodes_common_entities() {
        assertEquals("Tom & Jerry", HtmlText.clean("Tom &amp; Jerry"))
        assertEquals("\"quoted\"", HtmlText.clean("&quot;quoted&quot;"))
        assertEquals("it's", HtmlText.clean("it&#39;s"))
    }

    @Test
    fun collapses_whitespace() {
        assertEquals("a b c", HtmlText.clean("a    b\n\nc"))
    }

    @Test
    fun handles_null_and_blank() {
        assertEquals("", HtmlText.clean(null))
        assertEquals("", HtmlText.clean("   "))
    }
}
