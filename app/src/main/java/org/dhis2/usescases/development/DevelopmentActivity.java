package org.dhis2.usescases.development;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.DevelopmentActivityBinding;
import org.dhis2.commons.featureconfig.ui.FeatureConfigView;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.main.MainActivity;
import org.dhis2.utils.customviews.BreakTheGlassBottomDialog;
import org.dhis2.utils.dialFloatingActionButton.DialItem;
import org.hisp.dhis.android.core.D2Manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kotlin.Unit;

public class DevelopmentActivity extends ActivityGlobalAbstract {

    private int count;
    private List<String> iconNames;
    private DevelopmentActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.development_activity);

        loadAnalyticsDevTools();
        loadLocaleDevTools();
        loadIconsDevTools();
        loadBreakTheGlass();
        loadProgramRuleCheck();
        loadFeatureConfig();
    }

    private void loadProgramRuleCheck() {
        binding.ruleActionQualityButton.setOnClickListener(view -> {
            Intent intent = new Intent(this,ProgramRulesValidationActivity.class);
            startActivity(intent);
        });
    }

    private void loadAnalyticsDevTools() {
        binding.matomoButton.setOnClickListener(view -> {
            if (!binding.matomoUrl.getText().toString().isEmpty() &&
                    !binding.matomoId.getText().toString().isEmpty()) {
                ((App) getApplicationContext()).appComponent().matomoController().updateDhisImplementationTracker(
                        binding.matomoUrl.getText().toString(),
                        Integer.parseInt(binding.matomoId.getText().toString()),
                        "dev-tracker"
                );
            }
        });
    }

    private void loadLocaleDevTools() {
        binding.localeButton.setOnClickListener(view -> {
            if (binding.locale.getText().toString() != null) {
                String localeCode = binding.locale.getText().toString();
                Resources resources = getResources();
                DisplayMetrics dm = resources.getDisplayMetrics();
                Configuration config = resources.getConfiguration();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    config.setLocale(new Locale(localeCode.toLowerCase()));
                } else {
                    config.locale = new Locale(localeCode.toLowerCase());
                }
                resources.updateConfiguration(config, dm);
                startActivity(MainActivity.class, null, true, true, null);
            }
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

    private void loadBreakTheGlass() {
        binding.breakGlassButton.setOnClickListener(view ->
                new BreakTheGlassBottomDialog()
                        .setPositiveButton(reason -> {
                            Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
                            return Unit.INSTANCE;
                        })
                        .show(
                                getSupportFragmentManager(),
                                BreakTheGlassBottomDialog.class.getName()));
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
