/*
 *  Copyright (c) 2020 Private Internet Access, Inc.
 *
 *  This file is part of the Private Internet Access Android Client.
 *
 *  The Private Internet Access Android Client is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  The Private Internet Access Android Client is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with the Private
 *  Internet Access Android Client.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.privateinternetaccess.android.utils;

import android.content.res.XmlResourceParser;

import com.privateinternetaccess.android.model.draweritems.SettingsItem;
import com.privateinternetaccess.android.pia.utils.DLog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SettingsParseUtils {
    private final static String TAG_BASE = "preferences";
    private final static String TAG_CATEGORY = "category";
    private final static String TAG_ITEM = "item";

    private final static String TAG_TOGGLE = "toggle";
    private final static String TAG_DIALOG = "dialog";
    private final static String TAG_ACTION = "action";
    private final static String TAG_OPTIONS = "options_dialog";
    private final static String TAG_TEXT = "text_dialog";
    private final static String TAG_CUSTOM = "custom";

    private final static String VALUE_KEY = "key";
    private final static String VALUE_TITLE = "title";
    private final static String VALUE_SUMMARY = "summary";
    private final static String VALUE_TYPE = "type";

    public static List<SettingsItem> readSettings(XmlResourceParser parser) throws XmlPullParserException, IOException {
        List<SettingsItem> items = new ArrayList();
        int eventType = -1;

        while(eventType != parser.END_DOCUMENT) {
            if (eventType == XmlResourceParser.START_TAG) {
                String value = parser.getName();

                if (value.equals(TAG_CATEGORY)) {
                    items.addAll(readCategory(parser));
                }
            }

            eventType = parser.next();
        }
        DLog.d("SettingsParse", "Count: " + items.size());
        return items;
    }

    private static List<SettingsItem> readCategory(XmlResourceParser parser) throws XmlPullParserException, IOException {
        List<SettingsItem> items = new ArrayList();
        parser.require(XmlPullParser.START_TAG, null, TAG_CATEGORY);

        SettingsItem headerItem = new SettingsItem();
        headerItem.key = parser.getAttributeValue(null, VALUE_KEY);
        headerItem.title = parser.getAttributeResourceValue(null, VALUE_TITLE, 0);
        headerItem.setType(SettingsItem.SettingsType.CATEGORY);

        DLog.d("SettingsParse", "Item Title: " + headerItem.title);

        items.add(headerItem);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals(TAG_ITEM)) {
                items.add(readItem(parser));
            } else {
                skip(parser);
            }
        }
        return items;
    }

    private static SettingsItem readItem(XmlResourceParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, TAG_ITEM);

        SettingsItem item = new SettingsItem();
        item.key = parser.getAttributeValue(null, VALUE_KEY);
        item.title = parser.getAttributeResourceValue(null, VALUE_TITLE, 0);
        item.summary = parser.getAttributeResourceValue(null, VALUE_SUMMARY, 0);

        String type = parser.getAttributeValue(null, VALUE_TYPE);

        if (type.equals(TAG_TOGGLE)) {
            item.setType(SettingsItem.SettingsType.TOGGLE, parser);
        }

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
        }

        return item;
    }

    private static void skip(XmlResourceParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
