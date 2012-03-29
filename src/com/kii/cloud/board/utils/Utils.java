package com.kii.cloud.board.utils;

import java.io.InputStream;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.format.DateUtils;
import android.text.format.Time;

public class Utils {
    
    public static final String DEFAULT_ORDER = "date DESC";

    public static final String INTENT_TOPIC_ID = "topic";
    
    
    private static int ONE_MINUTE = 1000 * 60;
    private static int ONE_HOUR = ONE_MINUTE * 60;
    private static int ONE_DAY = ONE_HOUR * 24;
    
    public static String getContactName(Context context, String phoneNumber) {
        String number = phoneNumber;
        if(phoneNumber == null) return null;
        if (number.length() > 11)
            number = number.substring(number.length() - 11);
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };
        String selection = ContactsContract.CommonDataKinds.Phone.NUMBER
                + " LIKE \'%" + number + "\'";
        Cursor c = context.getContentResolver().query(uri, projection,
                selection, null, null);
        try {
            if (c == null || c.getCount() == 0)
                return phoneNumber;
            else {
                c.moveToFirst();
                return c.getString(0);
            }
        } finally {
            if (c != null)
                c.close();
        }

    }

    public static boolean isStared(Context context, String phoneNumber) {
        String number = phoneNumber;
        if (number.length() > 11)
            number = number.substring(number.length() - 11);
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.STARRED };
        String selection = ContactsContract.CommonDataKinds.Phone.NUMBER
                + " LIKE \'%" + number + "\'";
        Cursor c = context.getContentResolver().query(uri, projection,
                selection, null, null);
        try {
            if (c == null || c.getCount() == 0)
                return false;
            else {
                c.moveToFirst();
                return c.getInt(0) == 1 ? true : false;
            }
        } finally {
            if (c != null)
                c.close();
        }

    }

    public static Bitmap getPhoto(Context context, String phoneNumber) {
        String number = phoneNumber;
        if (number.length() > 11)
            number = number.substring(number.length() - 11);
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.CONTACT_ID };
        String selection = ContactsContract.CommonDataKinds.Phone.NUMBER
                + " LIKE \'%" + number + "\'";
        Cursor c = context.getContentResolver().query(uri, projection,
                selection, null, null);
        try {
            if (c == null || c.getCount() == 0)
                return null;
            else {
                c.moveToFirst();
                Uri photo_uri = ContentUris.withAppendedId(
                        ContactsContract.Contacts.CONTENT_URI,
                        Long.parseLong(c.getString(0)));
                InputStream input = ContactsContract.Contacts
                        .openContactPhotoInputStream(
                                context.getContentResolver(), photo_uri);
                if (input != null) {
                    Bitmap contactPhoto = BitmapFactory.decodeStream(input);
                    return contactPhoto;
                }
                return null;
            }
        } finally {
            if (c != null)
                c.close();
        }

    }
    
    public static String translateDateTime(long delta) {
        String displayTime = null;
        if (delta < 0) {
            return "";
        }

        if (delta > ONE_DAY) {
            int days = (int)delta / ONE_DAY;
            if (days > 1) {
                displayTime = days + " days ago";
            } else {
                displayTime = days + " day ago";
            }
        } else if (delta > ONE_HOUR) {
            int hours = (int)delta / ONE_HOUR;
            if (hours > 1) {
                displayTime = hours + " hours ago";
            } else {
                displayTime = hours + " hour ago";
            }
        } else {
            int mins = (int)delta / ONE_MINUTE;
            if (mins > 1) {
                displayTime = mins + " minutes ago";
            } else {
                displayTime = mins + " minute ago";
            }
        }
        return displayTime;
    }
    
    public static String formatTimeStampString(Context context, long when) {
        return formatTimeStampString(context, when, false);
    }

    public static String formatTimeStampString(Context context, long when, boolean fullFormat) {
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();
        
        

        // Basic settings for formatDateTime() we want for all cases.
        int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT |
                           DateUtils.FORMAT_ABBREV_ALL |
                           DateUtils.FORMAT_CAP_AMPM;

        // If the message is from a different year, show the date and year.
        if (then.year != now.year) {
            format_flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
        } else if (then.yearDay != now.yearDay) {
            // If it is from a different day than today, show only the date.
            format_flags |= DateUtils.FORMAT_SHOW_DATE;
        } else {
            // Otherwise, if the message is from today, show the time.
            format_flags |= DateUtils.FORMAT_SHOW_TIME;
        }

        // If the caller has asked for full details, make sure to show the date
        // and time no matter what we've determined above (but still make showing
        // the year only happen if it is a different year from today).
        if (fullFormat) {
            format_flags |= (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        }

        return DateUtils.formatDateTime(context, when, format_flags);
    }
}
