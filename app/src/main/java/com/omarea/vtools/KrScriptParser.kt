package com.omarea.vtools

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

data class ScriptNode(
    val title: String,
    val desc: String,
    val getStateScript: String,
    val setStateScript: String
)

object KrScriptParser {
    fun parse(input: InputStream): List<ScriptNode> {
        val list = mutableListOf<ScriptNode>()
        val parser = Xml.newPullParser()
        parser.setInput(input, "UTF-8")

        var event = parser.eventType
        var title = ""
        var desc = ""
        var getScript = ""
        var setScript = ""

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "switch", "action", "picker" -> {
                            title = parser.getAttributeValue(null, "title") ?: ""
                            desc = parser.getAttributeValue(null, "desc") ?: ""
                        }
                        "getstate", "get" -> getScript = parser.nextText().trim()
                        "setstate", "set" -> setScript = parser.nextText().trim()
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name in listOf("switch", "action", "picker")) {
                        list.add(ScriptNode(title, desc, getScript, setScript))
                        title = ""; desc = ""; getScript = ""; setScript = ""
                    }
                }
            }
            event = parser.next()
        }
        return list
    }
}
