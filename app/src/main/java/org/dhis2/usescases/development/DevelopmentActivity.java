package org.dhis2.usescases.development;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.R;
import org.dhis2.databinding.DevelopmentActivityBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 15/04/2019.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class DevelopmentActivity extends ActivityGlobalAbstract {

    private int count;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DevelopmentActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.development_activity);

        InputStream is = getResources().openRawResource(R.raw.icon_names);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try (Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        String json = writer.toString();
        List<String> iconNames = new Gson().fromJson(json, new TypeToken<List<String>>() {
        }.getType());
        count = 0;
        binding.iconButton.setOnClickListener(view -> {
            String iconName;
            if (count == iconNames.size())
                count = 0;

            if (iconNames.isEmpty()) {
                iconName = binding.iconInput.getText().toString();
            } else {
                iconName = iconNames.get(count);
                count++;
            }

            binding.iconInput.setText(iconName);

            int iconResourceNegative = getResources().getIdentifier(iconName + "_negative", "drawable", getPackageName());
            int iconResourceOutline = getResources().getIdentifier(iconName + "_outline", "drawable", getPackageName());
            int iconResourcePositive = getResources().getIdentifier(iconName + "_positive", "drawable", getPackageName());
            try {
                binding.iconInput.setError(null);
                binding.iconImagePossitive.setImageResource(iconResourcePositive);
                binding.iconImageOutline.setImageResource(iconResourceOutline);
                binding.iconImageNegative.setImageResource(iconResourceNegative);
            } catch (Exception e) {
                Timber.e(e);
                binding.iconInput.setError("This drawable has errors");
            }

        });

        binding.iconButtonBack.setOnClickListener(view -> {
            String iconName;
            if (count == 0)
                count = iconNames.size() - 1;
            if (iconNames.isEmpty() || count == 0) {
                count = 0;
                iconName = binding.iconInput.getText().toString();
            } else {
                iconName = iconNames.get(count);
                count--;
            }

            binding.iconInput.setText(iconName);

            int iconResourceNegative = getResources().getIdentifier(iconName + "_negative", "drawable", getPackageName());
            int iconResourceOutline = getResources().getIdentifier(iconName + "_outline", "drawable", getPackageName());
            int iconResourcePositive = getResources().getIdentifier(iconName + "_positive", "drawable", getPackageName());
            try {
                binding.iconInput.setError(null);
                binding.iconImagePossitive.setImageResource(iconResourcePositive);
                binding.iconImageOutline.setImageResource(iconResourceOutline);
                binding.iconImageNegative.setImageResource(iconResourceNegative);
            } catch (Exception e) {
                Timber.e(e);
                binding.iconInput.setError("This drawable has errors");
            }

        });

    }
}
