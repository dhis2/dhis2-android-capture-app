package org.dhis2.usescases.qrCodes;

import androidx.databinding.DataBindingUtil;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import android.view.View;

import com.bumptech.glide.Glide;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityQrCodesBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import static org.dhis2.data.qr.QRjson.ATTR_JSON;
import static org.dhis2.data.qr.QRjson.DATA_JSON;
import static org.dhis2.data.qr.QRjson.DATA_JSON_WO_REGISTRATION;
import static org.dhis2.data.qr.QRjson.ENROLLMENT_JSON;
import static org.dhis2.data.qr.QRjson.EVENTS_JSON;
import static org.dhis2.data.qr.QRjson.EVENT_JSON;
import static org.dhis2.data.qr.QRjson.RELATIONSHIP_JSON;
import static org.dhis2.data.qr.QRjson.TEI_JSON;

/**
 * QUADRAM. Created by ppajuelo on 21/06/2018.
 */

public class QrActivity extends ActivityGlobalAbstract implements QrContracts.View {

    @Inject
    public QrContracts.Presenter presenter;

    private ActivityQrCodesBinding binding;
    private QrAdapter qrAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new QrModule()).inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_qr_codes);
        binding.setName(getString(R.string.share_qr));
        binding.setPresenter(presenter);
        String teiUid = getIntent().getStringExtra("TEI_UID");

        qrAdapter = new QrAdapter(getSupportFragmentManager(), new ArrayList<>());
        binding.viewPager.setAdapter(qrAdapter);

        presenter.generateQrs(teiUid, this);
    }

    @Override
    protected void onStop() {
        presenter.onDetach();
        super.onStop();
    }

    @Override
    public void showQR(@NonNull List<QrViewModel> bitmaps) {

        qrAdapter.addItems(bitmaps);

        binding.setTitle(getString(R.string.qr_id));
        binding.page.setText(String.format(Locale.getDefault(), "1/%d", qrAdapter.getCount()));
        binding.prev.setVisibility(View.GONE);
        binding.next.setVisibility(View.VISIBLE);

        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // unused
            }

            @Override
            public void onPageSelected(int position) {
                binding.page.setText(String.format(Locale.getDefault(), "%d/%d", position + 1, bitmaps.size()));

                if (position + 1 == bitmaps.size()) {
                    binding.next.setVisibility(View.GONE);
                } else {
                    binding.next.setVisibility(View.VISIBLE);
                }

                if (position == 0) {
                    binding.prev.setVisibility(View.GONE);
                } else {
                    binding.prev.setVisibility(View.VISIBLE);
                }

                switch (bitmaps.get(position).getQrType()) {
                    case EVENT_JSON:
                        binding.setTitle(getString(R.string.qr_id));
                        break;
                    case DATA_JSON:
                    case DATA_JSON_WO_REGISTRATION:
                        binding.setTitle(getString(R.string.qr_data_values));
                        break;
                    case TEI_JSON:
                        binding.setTitle(getString(R.string.qr_id));
                        break;
                    case ATTR_JSON:
                        binding.setTitle(getString(R.string.qr_attributes));
                        break;
                    case ENROLLMENT_JSON:
                        binding.setTitle(getString(R.string.qr_enrollment));
                        break;
                    case EVENTS_JSON:
                        binding.setTitle(getString(R.string.qr_events));
                        break;
                    case RELATIONSHIP_JSON:
                        binding.setTitle(getString(R.string.qr_relationships));
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // unused
            }
        });
    }

    @Override
    public void onBackClick() {
        super.onBackPressed();
    }

    @Override
    public void onPrevQr() {
        binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() - 1);
    }

    @Override
    public void onNextQr() {
        binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
    }

    @Override
    public void showQRBitmap(Bitmap bitmap) {
        Glide.with(this).load(bitmap).into(binding.bitmapTest);
    }
}
