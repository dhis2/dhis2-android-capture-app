package org.dhis2.usescases.qrCodes;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.dhis2.data.qr.QRCodeGenerator;

import java.util.List;

public class QrAdapter extends FragmentStatePagerAdapter {

    @NonNull
    private final List<QrViewModel> bitmaps;

    public QrAdapter(@NonNull FragmentManager fragmentManager, @NonNull List<QrViewModel> bitmaps) {
        super(fragmentManager);
        this.bitmaps = bitmaps;
    }

    @Override
    public Fragment getItem(int position) {
        return QrFragment.create(QRCodeGenerator.transform(bitmaps.get(position).getQrType(), bitmaps.get(position).getQrJson()));
    }

    @Override
    public int getCount() {
        return bitmaps.size();
    }

    public void addItems(List<QrViewModel> bitmaps) {
        this.bitmaps.clear();
        this.bitmaps.addAll(bitmaps);
        notifyDataSetChanged();
    }

}