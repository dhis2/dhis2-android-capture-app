package org.dhis2.usescases.qrCodes;

import android.view.View;

import org.dhis2.R;
import org.dhis2.databinding.ActivityQrCodesBinding;
import org.dhis2.databinding.ActivityQrEventsWoRegistrationCodesBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import static org.dhis2.data.qr.QRjson.ATTR_JSON;
import static org.dhis2.data.qr.QRjson.DATA_JSON;
import static org.dhis2.data.qr.QRjson.DATA_JSON_WO_REGISTRATION;
import static org.dhis2.data.qr.QRjson.ENROLLMENT_JSON;
import static org.dhis2.data.qr.QRjson.EVENTS_JSON;
import static org.dhis2.data.qr.QRjson.EVENT_JSON;
import static org.dhis2.data.qr.QRjson.RELATIONSHIP_JSON;
import static org.dhis2.data.qr.QRjson.TEI_JSON;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public abstract class QrGlobalActivity extends ActivityGlobalAbstract {

    protected ActivityQrEventsWoRegistrationCodesBinding activityQrEventsWoRegistrationCodesBinding;
    protected ActivityQrCodesBinding activityQrCodesBinding;
    protected QrAdapter qrAdapter;

    public void showQR(@NonNull List<QrViewModel> bitmaps) {
        qrAdapter.addItems(bitmaps);
        if (activityQrCodesBinding != null) {
            setUpQrActivity(bitmaps);
        }
        if (activityQrEventsWoRegistrationCodesBinding != null) {
            setUpQrEventsWORegistrationActivity(bitmaps);
        }
    }

    private void setUpQrActivity(@NonNull List<QrViewModel> bitmaps) {
        activityQrCodesBinding.setTitle(getString(R.string.qr_id));
        activityQrCodesBinding.page.setText(String.format(Locale.getDefault(), "1/%d", qrAdapter.getCount()));
        activityQrCodesBinding.prev.setVisibility(View.GONE);
        activityQrCodesBinding.next.setVisibility(View.VISIBLE);

        activityQrCodesBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // unused
            }

            @Override
            public void onPageSelected(int position) {
                activityQrCodesBinding.page.setText(String.format(Locale.getDefault(), "%d/%d", position + 1, bitmaps.size()));

                if (position + 1 == bitmaps.size()) {
                    activityQrCodesBinding.next.setVisibility(View.GONE);
                } else {
                    activityQrCodesBinding.next.setVisibility(View.VISIBLE);
                }

                if (position == 0) {
                    activityQrCodesBinding.prev.setVisibility(View.GONE);
                } else {
                    activityQrCodesBinding.prev.setVisibility(View.VISIBLE);
                }

                switch (bitmaps.get(position).getQrType()) {
                    case EVENT_JSON:
                        activityQrCodesBinding.setTitle(getString(R.string.qr_id));
                        break;
                    case DATA_JSON:
                    case DATA_JSON_WO_REGISTRATION:
                        activityQrCodesBinding.setTitle(getString(R.string.qr_data_values));
                        break;
                    case TEI_JSON:
                        activityQrCodesBinding.setTitle(getString(R.string.qr_id));
                        break;
                    case ATTR_JSON:
                        activityQrCodesBinding.setTitle(getString(R.string.qr_attributes));
                        break;
                    case ENROLLMENT_JSON:
                        activityQrCodesBinding.setTitle(getString(R.string.qr_enrollment));
                        break;
                    case EVENTS_JSON:
                        activityQrCodesBinding.setTitle(getString(R.string.qr_events));
                        break;
                    case RELATIONSHIP_JSON:
                        activityQrCodesBinding.setTitle(getString(R.string.qr_relationships));
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

    private void setUpQrEventsWORegistrationActivity(@NonNull List<QrViewModel> bitmaps) {
        activityQrEventsWoRegistrationCodesBinding.setTitle(getString(R.string.qr_id));
        activityQrEventsWoRegistrationCodesBinding.page.setText(String.format(Locale.getDefault(), "1/%d", qrAdapter.getCount()));
        activityQrEventsWoRegistrationCodesBinding.prev.setVisibility(View.GONE);
        activityQrEventsWoRegistrationCodesBinding.next.setVisibility(View.VISIBLE);

        activityQrEventsWoRegistrationCodesBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // unused
            }

            @Override
            public void onPageSelected(int position) {
                activityQrEventsWoRegistrationCodesBinding.page.setText(String.format(Locale.getDefault(), "%d/%d", position + 1, bitmaps.size()));

                if (position + 1 == bitmaps.size()) {
                    activityQrEventsWoRegistrationCodesBinding.next.setVisibility(View.GONE);
                } else {
                    activityQrEventsWoRegistrationCodesBinding.next.setVisibility(View.VISIBLE);
                }

                if (position == 0) {
                    activityQrEventsWoRegistrationCodesBinding.prev.setVisibility(View.GONE);
                } else {
                    activityQrEventsWoRegistrationCodesBinding.prev.setVisibility(View.VISIBLE);
                }

                switch (bitmaps.get(position).getQrType()) {
                    case EVENT_JSON:
                        activityQrEventsWoRegistrationCodesBinding.setTitle(getString(R.string.qr_id));
                        break;
                    case DATA_JSON:
                    case DATA_JSON_WO_REGISTRATION:
                        activityQrEventsWoRegistrationCodesBinding.setTitle(getString(R.string.qr_data_values));
                        break;
                    case TEI_JSON:
                        activityQrEventsWoRegistrationCodesBinding.setTitle(getString(R.string.qr_id));
                        break;
                    case ATTR_JSON:
                        activityQrEventsWoRegistrationCodesBinding.setTitle(getString(R.string.qr_attributes));
                        break;
                    case ENROLLMENT_JSON:
                        activityQrEventsWoRegistrationCodesBinding.setTitle(getString(R.string.qr_enrollment));
                        break;
                    case EVENTS_JSON:
                        activityQrEventsWoRegistrationCodesBinding.setTitle(getString(R.string.qr_events));
                        break;
                    case RELATIONSHIP_JSON:
                        activityQrEventsWoRegistrationCodesBinding.setTitle(getString(R.string.qr_relationships));
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

    public void onBackClick() {
        super.onBackPressed();
    }

    public void onPrevQr() {
        if (activityQrCodesBinding != null)
            activityQrCodesBinding.viewPager.setCurrentItem(activityQrCodesBinding.viewPager.getCurrentItem() - 1);
        if (activityQrEventsWoRegistrationCodesBinding != null)
            activityQrEventsWoRegistrationCodesBinding.viewPager.setCurrentItem(activityQrEventsWoRegistrationCodesBinding.viewPager.getCurrentItem() - 1);
    }

    public void onNextQr() {
        if (activityQrCodesBinding != null)
            activityQrCodesBinding.viewPager.setCurrentItem(activityQrCodesBinding.viewPager.getCurrentItem() + 1);

        if (activityQrEventsWoRegistrationCodesBinding != null)
            activityQrEventsWoRegistrationCodesBinding.viewPager.setCurrentItem(activityQrEventsWoRegistrationCodesBinding.viewPager.getCurrentItem() + 1);
    }
}
