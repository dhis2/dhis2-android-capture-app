package com.dhis2.usescases.qrReader;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dhis2.R;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class QrReaderFragment extends FragmentGlobalAbstract implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    public QrReaderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mScannerView = new ZXingScannerView(getActivity());
        return mScannerView;
    }

    @Override
    public void handleResult(Result result) {

    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }




}
