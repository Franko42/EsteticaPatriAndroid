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
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
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
                        val isAlreadyRegistered = (normalized.isNotEmpty() && existingNormalizedPhones.any { com.esteticapatri.app.domain.arePhonesMatching(it, normalized) }) ||
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
        val resolver = context.contentResolver

        // 1. Intentar consultar directamente si la URI apunta a CommonDataKinds.Phone
        val phoneProjection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val directResult = runCatching {
            resolver.query(contactUri, phoneProjection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                    val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    val id = if (idIndex >= 0) cursor.getString(idIndex) ?: "" else ""
                    val name = if (nameIndex >= 0) cursor.getString(nameIndex)?.trim() ?: "" else ""
                    val number = if (numberIndex >= 0) cursor.getString(numberIndex)?.trim() ?: "" else ""
                    val normalized = number.filter(Char::isDigit)

                    if (name.isNotEmpty() || number.isNotEmpty()) {
                        PhoneContact(
                            id = id,
                            name = name,
                            phoneNumber = number,
                            normalizedPhone = normalized
                        )
                    } else null
                } else null
            }
        }.getOrNull()

        if (directResult != null && (directResult.name.isNotEmpty() || directResult.phoneNumber.isNotEmpty())) {
            return directResult
        }

        // 2. Si la URI apunta a ContactsContract.Contacts (general), resolver nombre y teléfono asociado
        return runCatching {
            var contactId = ""
            var contactName = ""

            val contactsProjection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
            )
            resolver.query(contactUri, contactsProjection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    contactId = if (idIdx >= 0) cursor.getString(idIdx) ?: "" else ""
                    contactName = if (nameIdx >= 0) cursor.getString(nameIdx)?.trim() ?: "" else ""
                }
            }

            var phoneNumber = ""
            if (contactId.isNotEmpty()) {
                val phoneQueryProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                resolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    phoneQueryProjection,
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(contactId),
                    null
                )?.use { phoneCursor ->
                    if (phoneCursor.moveToFirst()) {
                        val numIdx = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        phoneNumber = if (numIdx >= 0) phoneCursor.getString(numIdx)?.trim() ?: "" else ""
                    }
                }
            }

            if (contactName.isNotEmpty() || phoneNumber.isNotEmpty()) {
                PhoneContact(
                    id = contactId,
                    name = contactName,
                    phoneNumber = phoneNumber,
                    normalizedPhone = phoneNumber.filter(Char::isDigit)
                )
            } else null
        }.getOrNull()
    }
}
