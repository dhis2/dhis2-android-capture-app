package org.dhis2.usescases.teiDashboard.nfc_data;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.nfc.NFCManager;
import org.dhis2.data.qr.QRInterface;
import org.dhis2.databinding.ActivityNfcWriteTrackerBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class NfcDataWriteActivity extends ActivityGlobalAbstract {

    @Inject
    QRInterface qrInterface;

    private NFCManager nfcManager;
    private CompositeDisposable disposable;
    ActivityNfcWriteTrackerBinding binding;
    private Tag currentTag;
    private NfcAdapter nfcAdapter;
    private boolean deleteDataMode;
    private String teiUid;
    private boolean readMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new NfcDataWriteModule()).inject(this);
        super.onCreate(savedInstanceState);
        teiUid = getIntent().getExtras().getString("TEI_UID");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_nfc_write_tracker);
        nfcManager = new NFCManager(this);
        binding.mode.setOnCheckedChangeListener((buttonView, isChecked) -> deleteDataMode = isChecked);
        binding.readMode.setOnCheckedChangeListener((buttonView, isChecked) -> readMode = isChecked);
    }

    @Override
    protected void onResume() {
        super.onResume();
        disposable = new CompositeDisposable();
        nfcManager.verifyNFC();
        Intent nfcIntent = new Intent(this, this.getClass());
        nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
                                       You should specify only the ones that you need. */
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        IntentFilter[] intentFiltersArray = new IntentFilter[]{ndef,};
        String[][] techList = new String[1][1];
        techList[0][0] = MifareClassic.class.getName();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techList);
        disposable.add(
                nfcManager.requestProgressProcessor().onBackpressureBuffer()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                message -> binding.currentMessage.setText(message),
                                Timber::e
                        )
        );

        disposable.add(
                nfcManager.requestInitProcessor()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(init -> {
                                    if (!init) {
                                        binding.nfcBg.animate().scaleX(0.1f).scaleY(0.1f).setDuration(1500)
                                                .withEndAction(() -> binding.nfcBg.setVisibility(View.GONE));
                                    }
                                },
                                Timber::e)
        );
    }

    @Override
    protected void onPause() {
        disposable.clear();
        nfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        binding.currentMessage.setText("NFC card detected");
        binding.nfcBg.setVisibility(View.VISIBLE);
        binding.nfcBg.animate().scaleX(100).scaleY(100).setDuration(1500)
                .withEndAction(this::applyNFCAction);

        super.onNewIntent(intent);
    }

    private void applyNFCAction() {
        if (readMode) {
            String data = qrInterface.decompress(nfcManager.readTag(currentTag));
            binding.currentMessage.setText(data);
            qrInterface.saveData(data);
        } else if (deleteDataMode) {
            nfcManager.clearTag(currentTag);
            binding.currentMessage.setText("Card cleared");
        } else {
            disposable.add(qrInterface.getNFCData(teiUid)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bytes -> {
                        if (bytes != null && bytes.length <= 720) {
                            writeData(bytes);
                        } else if (bytes.length > 720)
                            binding.currentMessage.setText("Can't store more than 720b");
                    }, Timber::e)
            );

        }
    }

    private void writeData(byte[] message) {
        int totalSectors = (int) Math.ceil(Double.valueOf(message.length) / (16.0 * 3.0));
        for (int sector = 1; sector < totalSectors + 1; sector++) {
            for (int block = 0; block < 3; block++) {
                byte[] sub = new byte[16];
                for (int i = 0; i < 16; i++) {
                    int nextByte = i + 16 * (block + 3 * (sector - 1));
                    sub[i] = nextByte >= message.length ? 0 : message[nextByte];
                }
                nfcManager.writeTag(currentTag, sector, block, sub);
            }
        }
        nfcManager.finish();
    }

}
