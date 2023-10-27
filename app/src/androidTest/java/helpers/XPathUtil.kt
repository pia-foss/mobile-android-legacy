package com.privateinternetaccess.android.helpers

import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector
import com.privateinternetaccess.android.core.BaseUiAutomatorClass
import java.io.File
import org.dom4j.Document
import org.dom4j.io.SAXReader

object XPathUtil : BaseUiAutomatorClass() {

    fun findElementByXPath(xPathQuery : String) : UiObject? {
        val dumpFile = File.createTempFile("uiDump", ".xml")
        device.dumpWindowHierarchy(dumpFile)
        //parse - translating info what we are getting to something workable and manageable
        val reader = SAXReader()
        val document:Document = reader.read(dumpFile)
        val node = document.selectSingleNode(xPathQuery) ?: return null

        if(node != null){
            val resourceID = node.valueOf("@resource-id")
            val contentDesc = node.valueOf("@content-desc")
            val textValue = node.valueOf("@text")
            val className = node.valueOf("@class")
            val selector = UiSelector()
            if(resourceID.isNotEmpty()) selector.resourceId(resourceID)
            if(contentDesc.isNotEmpty()) selector.description(contentDesc)
            if(textValue.isNotEmpty()) selector.text(textValue)
            if(className.isNotEmpty()) selector.className(className)
            return device.findObject(selector)
        }
        return null
    }
}