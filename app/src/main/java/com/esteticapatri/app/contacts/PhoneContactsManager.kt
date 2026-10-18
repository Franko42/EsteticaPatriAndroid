package com.esteticapatri.app.contacts

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

data class PhoneContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val normalizedPhone: String,
    val isAlreadyRegistered: Boolean = false
)

object PhoneContactsManager {

    fun fetchPhoneContacts(
        context: Context,
        existingNormalizedPhones: Set<String> = emptySet(),
        existingNames: Set<String> = emptySet()
    ): List<PhoneContact> {
        val contactsList = mutableListOf<PhoneContact>()
        val seenKeys = mutableSetOf<String>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = runCatching {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE NOCASE ASC"
            )
        }.getOrNull()

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val id = if (idIndex >= 0) it.getString(idIndex) ?: "" else ""
                val rawName = if (nameIndex >= 0) it.getString(nameIndex) ?: "" else ""
                val rawNumber = if (numberIndex >= 0) it.getString(numberIndex) ?: "" else ""

                val name = rawName.trim()
                val phoneNumber = rawNumber.trim()
                val normalized = phoneNumber.filter(Char::isDigit)

                if (name.isNotEmpty() && phoneNumber.isNotEmpty()) {
                    val key = if (normalized.isNotEmpty()) normalized else name.lowercase()
                    if (!seenKeys.contains(key)) {
                        seenKeys.add(key)
                        val isAlreadyRegistered = (normalized.isNotEmpty() && existingNormalizedPhones.contains(normalized)) ||
                            existingNames.contains(name.lowercase())

                        contactsList.add(
                            PhoneContact(
                                id = id,
                                name = name,
                                phoneNumber = phoneNumber,
                                normalizedPhone = normalized,
                                isAlreadyRegistered = isAlreadyRegistered
                            )
                        )
                    }
                }
            }
        }

        return contactsList.sortedBy { it.name.lowercase() }
    }

    fun getContactFromUri(context: Context, contactUri: Uri): PhoneContact? {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val cursor = runCatching {
            context.contentResolver.query(contactUri, projection, null, null, null)
        }.getOrNull()

        cursor?.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                val id = if (idIndex >= 0) it.getString(idIndex) ?: "" else ""
                val name = if (nameIndex >= 0) it.getString(nameIndex)?.trim() ?: "" else ""
                val number = if (numberIndex >= 0) it.getString(numberIndex)?.trim() ?: "" else ""
                val normalized = number.filter(Char::isDigit)

                if (name.isNotEmpty() || number.isNotEmpty()) {
                    return PhoneContact(
                        id = id,
                        name = name,
                        phoneNumber = number,
                        normalizedPhone = normalized
                    )
                }
            }
        }
        return null
    }
}
