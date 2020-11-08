package com.llk.myapplication.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.llk.myapplication.bean.ContactUser;

import java.util.ArrayList;
import java.util.List;

public class ContactUtils {
    public static List<ContactUser> getAllContacts(Context context) {
        List<ContactUser> contacts = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (cursor == null) return contacts;

        while (cursor.moveToNext()) {
            //新建一个联系人实例
            ContactUser temp = new ContactUser();
            String contactId = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts._ID));
            //获取联系人姓名
            String name = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            temp.setName(name);

            //获取联系人电话号码
            Cursor phoneCursor = context.getContentResolver()
                    .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
                            null,
                            null);
            while (phoneCursor.moveToNext()) {
                String phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phone = phone.replace("-", "");
                phone = phone.replace(" ", "");
                temp.setPhone(phone);
            }

            //获取联系人备注信息
            Cursor noteCursor = context.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[]{ContactsContract.Data._ID, ContactsContract.CommonDataKinds.Nickname.NAME},
                    ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "='"
                            + ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE + "'",
                    new String[]{contactId}, null);
            if (noteCursor.moveToFirst()) {
                do {
                    String note = noteCursor.getString(noteCursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
                    temp.setNote(note);
                } while (noteCursor.moveToNext());
            }
            contacts.add(temp);

            //记得要把cursor给close掉
            phoneCursor.close();
            noteCursor.close();
        }

        cursor.close();

        return contacts;
    }
}