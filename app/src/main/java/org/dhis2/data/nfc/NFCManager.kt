package org.dhis2.data.nfc

import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareClassic.KEY_DEFAULT
import android.nfc.tech.MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY
import android.nfc.tech.MifareClassic.KEY_NFC_FORUM
import androidx.annotation.NonNull
import io.reactivex.Flowable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor

class NFCManager internal constructor(private val context: Context) {

    private lateinit var nfcAdt: NfcAdapter
    private var nfcProcessor: FlowableProcessor<String>? = null
    private var initProcessor: FlowableProcessor<Boolean>? = null

    fun verifyNFC(): Boolean {
        nfcAdt = NfcAdapter.getDefaultAdapter(context)
        if (nfcAdt == null) {
            throw NFcNotEnabled()
        }

        if (!nfcAdt.isEnabled) {
            return false
        }

        return true
    }

    fun requestProgressProcessor(): Flowable<String> {
        if (nfcProcessor == null) {
            nfcProcessor = PublishProcessor.create()
        }

        return nfcProcessor!!
    }

    fun requestInitProcessor(): Flowable<Boolean> {
        if (initProcessor == null) {
            initProcessor = PublishProcessor.create()
        }

        return initProcessor!!
    }

    fun clearTag(tag: Tag?) {
        for (sector in 1 until 16) {
            for (block in 0 until 3) {
                val emptyData = ByteArray(16)
                for (i in 0 until 16)
                    emptyData[i] = 0
                writeTag(tag, sector, block, emptyData)
            }
        }
        sendMessage("All sectors cleared")
        initProcessor?.onNext(false)
    }

    fun writeTag(tag: Tag?, sector: Int, block: Int, @NonNull subData: ByteArray): Boolean {
        if (tag != null) {
            try {
                val mifareTag = MifareClassic.get(tag)
                if (!mifareTag.isConnected) {
                    mifareTag.connect()
                }
                var auth = false
                while (!auth)
                    if (mifareTag.authenticateSectorWithKeyA(
                        sector, KEY_MIFARE_APPLICATION_DIRECTORY
                    )
                    ) {
                        auth = true
                    } else if (mifareTag.authenticateSectorWithKeyA(
                        sector,
                        KEY_DEFAULT
                    )
                    ) {
                        auth = true
                    } else if (mifareTag.authenticateSectorWithKeyA(
                        sector,
                        KEY_NFC_FORUM
                    )
                    ) {
                        auth = true
                    }

                if (auth) {
                    mifareTag.writeBlock(mifareTag.sectorToBlock(sector) + block, subData)
                    sendMessage("Data write success")
                }

                mifareTag.close()

                return auth
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        } else {
            return false
        }
    }

    fun sendMessage(text: String) = nfcProcessor?.let { it.onNext(text) }

    fun readTag(tag: Tag): ByteArray? {
        if (tag != null) {
            val data = ByteArray(720)
            for (sector in 1 until 16) {
                for (block in 0 until 3) {
                    val blockByte = getBlockByte(tag, sector, block)
                    for (i in 0 until 16) {
                        var nextByte = i + 16 * (block + 3 * (sector - 1))
                        data[nextByte] = blockByte[i]
                    }
                }
            }
            initProcessor?.onNext(false)
            return data
        } else {
            return null
        }
    }

    private fun getBlockByte(tag: Tag, sector: Int, block: Int): ByteArray {
        try {
            val mifareTag = MifareClassic.get(tag)
            if (!mifareTag.isConnected) {
                mifareTag.connect()
            }

            var auth = false
            while (!auth)
                when {
                    mifareTag.authenticateSectorWithKeyA(
                        sector,
                        KEY_MIFARE_APPLICATION_DIRECTORY
                    ) ->
                        auth = true
                    mifareTag.authenticateSectorWithKeyA(sector, KEY_DEFAULT) -> auth = true
                    mifareTag.authenticateSectorWithKeyA(sector, KEY_NFC_FORUM) -> auth = true
                }

            var rn = ByteArray(16)
            if (auth) {
                rn = mifareTag.readBlock(mifareTag.sectorToBlock(sector) + block)
            }

            mifareTag.close()

            return rn
        } catch (e: Exception) {
            e.printStackTrace()
            return ByteArray(16)
        }
    }

    fun finish() {
        initProcessor?.onNext(false)
    }
}
