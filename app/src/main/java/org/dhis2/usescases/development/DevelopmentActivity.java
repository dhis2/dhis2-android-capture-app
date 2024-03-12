package org.dhis2.usescases.development;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.R;
import org.dhis2.commons.featureconfig.ui.FeatureConfigView;
import org.dhis2.databinding.DevelopmentActivityBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.D2Manager;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.fileresource.FileResource;
import org.hisp.dhis.android.core.fileresource.FileResourceDomain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

public class DevelopmentActivity extends ActivityGlobalAbstract {

    private int count;
    private List<String> iconNames;
    private DevelopmentActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.development_activity);

        loadIconsDevTools();
        loadCrashControl();
        loadFeatureConfig();
        loadConflicts();
        loadMultiText();
        loadCustomIcons();
    }

    private void loadCustomIcons() {
        binding.forceCustomIcon.setOnClickListener(view -> {
            D2 d2 = D2Manager.getD2();
            FileResource fileResource = d2.fileResourceModule().fileResources()
                    .byDomain().eq(FileResourceDomain.DATA_VALUE)
                    .one().blockingGet();
            if (fileResource != null) {
                String uidToInsert = fileResource.uid();
                d2.databaseAdapter().execSQL(
                        String.format(
                                "INSERT INTO CustomIcon (\"key\", \"fileResourceUid\", \"href\") VALUES (\"%s\",\"%s\",\"%s\")",
                                uidToInsert,
                                uidToInsert,
                                uidToInsert
                        )
                );
                d2.databaseAdapter().execSQL(
                        String.format("UPDATE Program SET icon = \"%s\"", uidToInsert)
                );
                d2.databaseAdapter().execSQL(
                        String.format("UPDATE DataSet SET icon = \"%s\"", uidToInsert)
                );
                d2.databaseAdapter().execSQL(
                        String.format("UPDATE ProgramStage SET icon = \"%s\"", uidToInsert)
                );
                d2.databaseAdapter().execSQL(
                        String.format("UPDATE Option SET icon = \"%s\"", uidToInsert)
                );
            } else {
                Toast.makeText(this, "No file resource found. Add an image in a form and retry", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void loadMultiText() {
        D2 d2 = D2Manager.getD2();
        boolean hasMultiText = !d2.dataElementModule().dataElements().byValueType().eq(ValueType.MULTI_TEXT).blockingIsEmpty();
        binding.multitext.setText(hasMultiText ? "REVERT" : "FORCE MULTITEXT");
        binding.multitext.setOnClickListener(view -> {
            if (hasMultiText) {
                d2.databaseAdapter().execSQL(
                        "UPDATE DataElement SET valueType = \"TEXT\" WHERE valueType = \"MULTI_TEXT\" AND optionSet IS NOT null"
                );
            } else {
                d2.databaseAdapter().execSQL(
                        "UPDATE DataElement SET valueType = \"MULTI_TEXT\" WHERE valueType = \"TEXT\" AND optionSet IS NOT null"
                );
            }
        });
    }

    private void loadConflicts() {
        binding.addConflicts.setOnClickListener(view -> {
            D2 d2 = D2Manager.getD2();
            new ConflictGenerator(d2).generate();
        });
        binding.clearConflicts.setOnClickListener(view -> {
            D2 d2 = D2Manager.getD2();
            new ConflictGenerator(d2).clear();
        });
    }

    private void loadIconsDevTools() {
        InputStream is = getResources().openRawResource(R.raw.icon_names);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String json = writer.toString();
        iconNames = new Gson().fromJson(json, new TypeToken<List<String>>() {
        }.getType());
        count = 0;

        binding.iconButton.setOnClickListener(view -> {
            nextDrawable();
        });

        binding.automaticErrorCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                nextDrawable();
        });

        renderIconForPosition(count);
    }

    private void renderIconForPosition(int position) {
        String iconName = iconNames.get(position);

        binding.iconInput.setText(iconName);

        int iconResource_negative = getResources().getIdentifier(iconName + "_negative", "drawable", getPackageName());
        int iconResource_outline = getResources().getIdentifier(iconName + "_outline", "drawable", getPackageName());
        int iconResource_positive = getResources().getIdentifier(iconName + "_positive", "drawable", getPackageName());
        binding.iconInput.setError(null);

        binding.iconImagePossitive.setImageDrawable(null);
        binding.iconImageOutline.setImageDrawable(null);
        binding.iconImageNegative.setImageDrawable(null);

        binding.iconImagePossitiveTint.setImageDrawable(null);
        binding.iconImageOutlineTint.setImageDrawable(null);
        binding.iconImageNegativeTint.setImageDrawable(null);

        boolean hasError = false;
        try {
            binding.iconImagePossitive.setImageResource(iconResource_positive);

        } catch (Exception e) {
            e.printStackTrace();
            hasError = true;
        }

        try {
            binding.iconImageOutline.setImageResource(iconResource_outline);
        } catch (Exception e) {
            e.printStackTrace();
            hasError = true;
        }

        try {
            binding.iconImageNegative.setImageResource(iconResource_negative);
        } catch (Exception e) {
            e.printStackTrace();
            hasError = true;
        }

        try {
            binding.iconImagePossitiveTint.setImageResource(iconResource_positive);

        } catch (Exception e) {
            e.printStackTrace();
            hasError = true;
        }

        try {

            binding.iconImageOutlineTint.setImageResource(iconResource_outline);

        } catch (Exception e) {
            e.printStackTrace();
            hasError = true;
        }

        try {
            binding.iconImageNegativeTint.setImageResource(iconResource_negative);

        } catch (Exception e) {
            e.printStackTrace();
            hasError = true;
        }


        if (hasError) {
            binding.iconInput.setError("This drawable has errors");
        } else if (binding.automaticErrorCheck.isChecked()) {
            nextDrawable();
        }
    }

    private void nextDrawable() {
        count++;
        if (count == iconNames.size()) {
            count = 0;
            binding.automaticErrorCheck.setChecked(false);
            return;
        }
        renderIconForPosition(count);
    }

    private void loadCrashControl() {
        binding.crashButton.setOnClickListener(view -> {
            throw new IllegalArgumentException("KA BOOOOOM!");
        });
    }

    private void loadFeatureConfig() {
        binding.featureConfigButton.setOnClickListener(view -> {
            startActivity(FeatureConfigView.class, null, false, false, null);
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }
}
