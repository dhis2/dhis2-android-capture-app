package org.dhis2.utils.custom_views;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormPictureAccentBinding;
import org.dhis2.databinding.FormPictureBinding;
import org.dhis2.utils.FilderUtil;

import java.io.File;
import java.util.Locale;

import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

public class PictureView extends FieldLayout implements View.OnClickListener, View.OnFocusChangeListener {

    private ViewDataBinding binding;
    private FlowableProcessor<RowAction> processor;
    private String uid;
    private TextView errorView;
    private ImageView image;
    private OnIntentSelected onIntentSelected;

    public PictureView(Context context) {
        super(context);
        init(context);
    }

    public PictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PictureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void onClick(View v) {
        if (v == image) {
            selectImage();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

    @Override
    public void performOnFocusAction() {

    }

    private void setLayout() {
        if (isBgTransparent)
            binding = DataBindingUtil.inflate(inflater, R.layout.form_picture, this, true);
        else
            binding = DataBindingUtil.inflate(inflater, R.layout.form_picture_accent, this, true);

        errorView = findViewById(R.id.errorMessage);
        image = findViewById(R.id.image);
        image.setOnClickListener(this);
    }

    public void setLabel(String label) {
        this.label = label;
        if (binding instanceof FormPictureBinding)
            ((FormPictureBinding) binding).setLabel(label);
        else
            ((FormPictureAccentBinding) binding).setLabel(label);
    }

    public void setDescription(String description) {
        if (binding instanceof FormPictureBinding)
            ((FormPictureBinding) binding).setDescription(description);
        else
            ((FormPictureAccentBinding) binding).setDescription(description);
    }

    public void setWarning(String msg) {
        if (!isEmpty(msg)) {
            errorView.setTextColor(ContextCompat.getColor(getContext(), R.color.warning_color));
            errorView.setText(msg);
            errorView.setVisibility(VISIBLE);
        } else
            errorView.setVisibility(GONE);
    }

    public void setError(String msg) {
        if (!isEmpty(msg)) {
            errorView.setTextColor(ContextCompat.getColor(getContext(), R.color.error_color));
            errorView.setText(msg);
            errorView.setVisibility(VISIBLE);
        } else
            errorView.setVisibility(GONE);
    }

    public void setIsBgTransparent(boolean isBgTransparent) {
        this.isBgTransparent = isBgTransparent;
        setLayout();
    }

    public void setProcessor(String uid, FlowableProcessor<RowAction> processor) {
        this.processor = processor;
        this.uid = uid;
    }

    public void setInitialValue(String value) {
        File file = FilderUtil.getFilePath(value, image.getContext());
        if(file.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            image.setImageBitmap(myBitmap);
        }
    }




    private void selectImage() {
        try {
            PackageManager pm = image.getContext().getPackageManager();
            int hasPerm = pm.checkPermission(Manifest.permission.CAMERA, image.getContext().getPackageName());
            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                final CharSequence[] options = {"Take Photo", "Choose From Gallery","Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(image.getContext());
                builder.setTitle("Select Option");
                builder.setItems(options, (dialog, item) -> {
                    if (options[item].equals("Take Photo")) {
                        dialog.dismiss();
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        onIntentSelected.intentSelected(this, this.uid, intent, 100, (value, uuid) -> {
                            if (this.uid.equals(uuid)) {
                                setInitialValue(value);
                                if (uid != null) {
                                    processor.onNext(
                                            RowAction.create(uid,
                                                    value)
                                    );
                                    nextFocus(this);
                                }
                            }
                        });
                    } else if (options[item].equals("Choose From Gallery")) {
                        dialog.dismiss();
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        onIntentSelected.intentSelected(this, this.uid, pickPhoto, 200, (value, uuid) -> {
                            if (this.uid.equals(uuid)) {
                                setInitialValue(value);
                                if (uid != null) {
                                    processor.onNext(
                                            RowAction.create(uid,
                                                    value)
                                    );
                                    nextFocus(this);
                                }
                            }
                        });
                    } else if (options[item].equals("Cancel")) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            } else
                Toast.makeText(image.getContext(), "Camera Permission error", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(image.getContext(), "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public interface OnPictureSelected {
        public void onSelected(String value, String uuid);
    }

    public interface OnIntentSelected {
        void intentSelected(PictureView pictureView, String uuid, Intent intent, int request, OnPictureSelected onPictureSelected);
    }


    public void setOnIntentSelected(OnIntentSelected onIntentSelected) {
        this.onIntentSelected = onIntentSelected;
    }


}
