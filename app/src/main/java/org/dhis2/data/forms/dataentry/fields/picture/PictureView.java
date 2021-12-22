package org.dhis2.data.forms.dataentry.fields.picture;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;

import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.FileExtensionsKt;
import org.dhis2.BuildConfig;
import org.dhis2.R;
import org.dhis2.databinding.FormPictureAccentBinding;
import org.dhis2.databinding.FormPictureBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.ActivityResultObservable;
import org.dhis2.utils.ActivityResultObserver;
import org.dhis2.utils.Constants;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.utils.customviews.FieldLayout;
import org.dhis2.utils.customviews.ImageDetailBottomDialog;
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import kotlin.Pair;

import static android.app.Activity.RESULT_OK;
import static android.text.TextUtils.isEmpty;

public class PictureView extends FieldLayout implements View.OnClickListener, ActivityResultObserver {

    public static final int PERMISSION_REQUEST = 179;

    private ViewDataBinding binding;
    private String uid;
    private TextView errorView;
    private ImageView image;
    private MaterialButton addImageBtn;
    private String primaryUid;
    private Boolean isEditable;
    private ImageButton clearButton;
    private String currentValue;
    private CardView imageCard;
    private final FragmentManager supportFragmentManager = ((FragmentActivity) getContext()).getSupportFragmentManager();
    private PictureViewModel viewModel;
    private boolean isPermissionRequested = false;

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
        if (isEditable && v == addImageBtn) {
            viewModel.onItemClick();
            checkPermissions();
        }
    }

    private void setLayout() {
        if (isBgTransparent)
            binding = DataBindingUtil.inflate(inflater, R.layout.form_picture, this, true);
        else
            binding = DataBindingUtil.inflate(inflater, R.layout.form_picture_accent, this, true);

        errorView = findViewById(R.id.errorMessage);
        image = findViewById(R.id.image);
        image.setOnClickListener(view -> {
            viewModel.onItemClick();
            showFullPicture();
        });
        addImageBtn = findViewById(R.id.addImageBtn);
        imageCard = findViewById(R.id.imageCard);
        addImageBtn.setOnClickListener(this);
        clearButton = findViewById(R.id.clear);
        clearButton.setOnClickListener(view -> {
                    viewModel.onItemClick();
                    if (isEditable && removeFile()) {
                        addImageBtn.setVisibility(VISIBLE);
                        imageCard.setVisibility(View.GONE);
                        Glide.with(this).clear(image);
                        viewModel.onClearValue();
                    }
                }
        );
    }

    private boolean removeFile() {
        return currentValue != null && new File(currentValue).delete();
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

        findViewById(R.id.descriptionLabel).setOnClickListener(v ->
                new CustomDialog(
                        getContext(),
                        label,
                        description != null ? description : getContext().getString(R.string.empty_description),
                        getContext().getString(R.string.action_close),
                        null,
                        Constants.DESCRIPTION_DIALOG,
                        null
                ).show());
    }

    public void setWarningErrorMessage(String warning, String error) {
        if (!isEmpty(error)) {
            errorView.setTextColor(ContextCompat.getColor(getContext(), R.color.error_color));
            errorView.setText(error);
            errorView.setVisibility(VISIBLE);
        } else if (!isEmpty(warning)) {
            errorView.setTextColor(ContextCompat.getColor(getContext(), R.color.warning_color));
            errorView.setText(warning);
            errorView.setVisibility(VISIBLE);
        } else {
            errorView.setVisibility(GONE);
        }
    }

    public void setIsBgTransparent(boolean isBgTransparent) {
        this.isBgTransparent = isBgTransparent;
        setLayout();
    }

    public void setProcessor(String primaryUid, String uid) {
        this.primaryUid = primaryUid;
        this.uid = uid;
    }

    public void setInitialValue(String value) {

        if (!isEmpty(value)) {

            Glide.with(image).clear(image);

            File file = new File(value);

            if (file.exists()) {
                Pair<Integer, Integer> dimensions = FileExtensionsKt.widthAndHeight(file, ExtensionsKt.getDp(200));
                currentValue = value;
                addImageBtn.setVisibility(GONE);
                imageCard.setVisibility(View.VISIBLE);
                Glide.with(image)
                        .load(file)
                        .apply(new RequestOptions().centerCrop())
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(ExtensionsKt.getDp(6))))
                        .apply(RequestOptions.overrideOf(dimensions.component1(), dimensions.component2()))
                        .skipMemoryCache(true)
                        .into(image);
                clearButton.setVisibility(VISIBLE);
            }
        } else
            clearButton.setVisibility(View.GONE);
    }


    private void selectImage() {
        final CharSequence[] options = {
                getContext().getString(R.string.take_photo),
                getContext().getString(R.string.from_gallery),
                getContext().getString(R.string.cancel)
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(image.getContext());
        builder.setTitle(getContext().getString(R.string.select_option));
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals( getContext().getString(R.string.take_photo))) {
                dialog.dismiss();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri photoUri = FileProvider.getUriForFile(getContext(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        new File(FileResourceDirectoryHelper.getFileResourceDirectory(getContext()), "tempFile.png"));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                ((ActivityGlobalAbstract) getContext()).uuid = uid;
                ((FragmentActivity) getContext()).startActivityForResult(intent, Constants.CAMERA_REQUEST);
            } else if (options[item].equals( getContext().getString(R.string.from_gallery))) {
                dialog.dismiss();
                Intent pickPhoto = new Intent(Intent.ACTION_PICK);
                pickPhoto.putExtra("filename", primaryUid.concat("_").concat(uid));
                pickPhoto.setType("image/*");
                ((ActivityGlobalAbstract) getContext()).uuid = uid;
                ((FragmentActivity) getContext()).startActivityForResult(pickPhoto, Constants.GALLERY_REQUEST);
            } else if (options[item].equals(getContext().getString(R.string.cancel))) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showFullPicture() {
        new ImageDetailBottomDialog(label, new File(currentValue))
                .show(supportFragmentManager, ImageDetailBottomDialog.TAG);
    }

    public void setEditable(Boolean editable) {
        isEditable = editable;
        addImageBtn.setEnabled(editable);
        setEditable(editable,
                findViewById(R.id.label),
                findViewById(R.id.descriptionLabel),
                addImageBtn,
                clearButton
        );
    }

    private void checkPermissions() {
        subscribe();
        if (ContextCompat.checkSelfPermission(((ActivityGlobalAbstract) getContext()), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(((ActivityGlobalAbstract) getContext()), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        } else if (!isPermissionRequested) {
            isPermissionRequested = true;
            ActivityCompat.requestPermissions(((ActivityGlobalAbstract) getContext()),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    PERMISSION_REQUEST);
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.camera_permission_denied), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) {
            checkPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            }  else {
                Toast.makeText(getContext(), getContext().getString(R.string.camera_permission_denied), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void setViewModel(PictureViewModel viewModel) {
        this.viewModel = viewModel;

        if (binding == null) {
            setIsBgTransparent(viewModel.isBackgroundTransparent());
        }
        setProcessor(viewModel.uid().contains("_") ? viewModel.uid().split("_")[0] : viewModel.uid(),
                viewModel.uid().contains("_") ? viewModel.uid().split("_")[1] : viewModel.uid());
        setLabel(viewModel.getFormattedLabel());
        setDescription(viewModel.description());
        setInitialValue(viewModel.value());
        setEditable(viewModel.editable());
        setWarningErrorMessage(viewModel.warning(), viewModel.error());
    }

    private void subscribe() {
        ((ActivityResultObservable) getContext()).subscribe(this);
    }
}
