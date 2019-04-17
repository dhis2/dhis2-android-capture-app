package org.dhis2.usescases.development;

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.R;
import org.dhis2.databinding.DevelopmentActivityBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

/**
 * QUADRAM. Created by ppajuelo on 15/04/2019.
 */
public class DevelopmentActivity extends ActivityGlobalAbstract {

    private int count;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DevelopmentActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.development_activity);

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

            int iconResource_negative = getResources().getIdentifier(iconName + "_negative", "drawable", getPackageName());
            int iconResource_outline = getResources().getIdentifier(iconName + "_outline", "drawable", getPackageName());
            int iconResource_positive = getResources().getIdentifier(iconName + "_positive", "drawable", getPackageName());
            try {
                binding.iconInput.setError(null);
                binding.iconImagePossitive.setImageResource(iconResource_positive);
                binding.iconImageOutline.setImageResource(iconResource_outline);
                binding.iconImageNegative.setImageResource(iconResource_negative);
            } catch (Exception e) {
                e.printStackTrace();
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

            int iconResource_negative = getResources().getIdentifier(iconName + "_negative", "drawable", getPackageName());
            int iconResource_outline = getResources().getIdentifier(iconName + "_outline", "drawable", getPackageName());
            int iconResource_positive = getResources().getIdentifier(iconName + "_positive", "drawable", getPackageName());
            try {
                binding.iconInput.setError(null);
                binding.iconImagePossitive.setImageResource(iconResource_positive);
                binding.iconImageOutline.setImageResource(iconResource_outline);
                binding.iconImageNegative.setImageResource(iconResource_negative);
            } catch (Exception e) {
                e.printStackTrace();
                binding.iconInput.setError("This drawable has errors");
            }

        });

    }
}
