package org.dhis2.utils.custom_views;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputEditText;

import org.dhis2.BuildConfig;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormPictureAccentBinding;
import org.dhis2.databinding.FormPictureBinding;
import org.dhis2.utils.Constants;
import org.dhis2.utils.FileResourcesUtil;

import java.io.File;

import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

public class PictureView extends FieldLayout implements View.OnClickListener, View.OnFocusChangeListener {

    private ViewDataBinding binding;
    private String uid;
    private TextView errorView;
    private ImageView image;
    private LinearLayout layout;
    private TextInputEditText formLabel;
    private OnIntentSelected onIntentSelected;
    private String primaryUid;
    private OnPictureSelected imageListener;
    private Boolean isEditable;
    private View clearButton;

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
        if (isEditable && (v == image || v == layout || v == formLabel)) {
            selectImage();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

    private void setLayout() {
        if (isBgTransparent)
            binding = DataBindingUtil.inflate(inflater, R.layout.form_picture, this, true);
        else
            binding = DataBindingUtil.inflate(inflater, R.layout.form_picture_accent, this, true);

        errorView = findViewById(R.id.errorMessage);
        image = findViewById(R.id.image);
        image.setOnClickListener(this);
        layout = findViewById(R.id.layout);
        layout.setOnClickListener(this);
        formLabel = findViewById(R.id.formLabel);
        formLabel.setOnClickListener(this);
        clearButton = findViewById(R.id.clear);
        clearButton.setOnClickListener(view -> {
                    if (removeFile()) {
                        setTextSelected(null);
                        image.setVisibility(View.GONE);
                        Glide.with(this).clear(image);
                        imageListener.onSelected(null, null, uid);
                    }
                }
        );
    }

    private boolean removeFile() {
        return FileResourcesUtil.getFileForAttribute(getContext(), primaryUid.concat("_").concat(uid).concat(".png")).delete();
    }

    public void setLabel(String label) {
        this.label = label;
        if (binding instanceof FormPictureBinding)
            ((FormPictureBinding) binding).setLabel(label);
        else
            ((FormPictureAccentBinding) binding).setLabel(label);
    }

    public void setTextSelected(String text) {
        if (binding instanceof FormPictureBinding)
            ((FormPictureBinding) binding).setSelected(text);
        else
            ((FormPictureAccentBinding) binding).setLabel(text);
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

    public void setProcessor(String primaryUid, String uid, FlowableProcessor<RowAction> processor) {
        this.primaryUid = primaryUid;
        this.uid = uid;
    }

    public void setInitialValue(String value) {

        if (!isEmpty(value)) {

            Glide.with(image).clear(image);
            clearButton.setVisibility(View.VISIBLE);

//            File file = FileResourcesUtil.getFileForAttribute(getContext(), primaryUid.concat("_").concat(uid).concat(".png"));
            File file = new File(value);

            if (file.exists()) {
                setTextSelected(getContext().getString(R.string.image_selected));
                image.setVisibility(View.VISIBLE);
                Glide.with(image)
                        .load(file)
                        .apply(new RequestOptions().centerCrop())
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                        .skipMemoryCache(true)
                        .into(image);
            }
        } else
            clearButton.setVisibility(View.GONE);
    }


    private void selectImage() {
        try {
            PackageManager pm = image.getContext().getPackageManager();
            int hasPerm = pm.checkPermission(Manifest.permission.CAMERA, image.getContext().getPackageName());
            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                final CharSequence[] options = {"Take Photo", "Choose From Gallery", "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(image.getContext());
                builder.setTitle("Select Option");
                builder.setItems(options, (dialog, item) -> {
                    if (options[item].equals("Take Photo")) {
                        dialog.dismiss();
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        Uri photoUri = FileProvider.getUriForFile(getContext(),
                                BuildConfig.APPLICATION_ID + ".provider",
                                new File(FileResourcesUtil.getUploadDirectory(getContext()), "tempFile.png"));
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        PictureView.this.onIntentSelected.intentSelected(this.uid, intent, Constants.CAMERA_REQUEST, (file, value, uuid) -> {
                            if (value != null) {
                                imageListener.onSelected(file, primaryUid.concat("_").concat(uid), uid);
                            } else {
                                File file2 = new File(FileResourcesUtil.getUploadDirectory(getContext()), FileResourcesUtil.generateFileName(primaryUid, uid));
                                if (file2.exists())
                                    imageListener.onSelected(file2, primaryUid.concat("_").concat(uid), uid);
                            }
                        });
                    } else if (options[item].equals("Choose From Gallery")) {
                        dialog.dismiss();
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK);
                        pickPhoto.putExtra("filename", primaryUid.concat("_").concat(uid));
                        pickPhoto.setType("image/*");
                        PictureView.this.onIntentSelected.intentSelected(this.uid, pickPhoto, Constants.GALLERY_REQUEST, (file, value, uuid) -> {
                            FileResourcesUtil.saveImageToUpload(file, new File(FileResourcesUtil.getUploadDirectory(getContext()), FileResourcesUtil.generateFileName(primaryUid, uid)));
                            imageListener.onSelected(file, primaryUid.concat("_").concat(uid), uid);
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

    public void setOnImageListener(OnPictureSelected onImageListener) {
        this.imageListener = onImageListener;
    }

    public void setEditable(Boolean editable) {
        isEditable = editable;
    }

    public interface OnPictureSelected {
        void onSelected(File file, String value, String uid);
    }

    public interface OnIntentSelected {
        void intentSelected(String uuid, Intent intent, int request, OnPictureSelected onPictureSelected);
    }


    public void setOnIntentSelected(OnIntentSelected onIntentSelected) {
        this.onIntentSelected = onIntentSelected;
    }


}
