package com.dhis2.usescases.general;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.widget.Toast;

import com.dhis2.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.hisp.dhis.android.core.enrollment.note.NoteModel;

import java.lang.reflect.Type;
import java.util.List;

import io.reactivex.functions.Consumer;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ppajuelo on 18/10/2017.
 *
 */

public abstract class FragmentGlobalAbstract extends android.support.v4.app.Fragment implements AbstractActivityContracts.View {
    public ViewDataBinding binding;


    @Override
    public void startActivity(@NonNull Class<?> destination, @Nullable Bundle bundle, boolean finishCurrent, boolean finishAll, @Nullable ActivityOptionsCompat transition) {
        getAbstracContext().startActivity(destination, bundle, finishCurrent, finishAll, transition);
    }

    @Override
    public ActivityGlobalAbstract getAbstractActivity() {
        return (ActivityGlobalAbstract) getActivity();
    }

    @Override
    public void back() {
        getAbstracContext().back();
    }

    @Override
    public ActivityGlobalAbstract getAbstracContext() {
        return (ActivityGlobalAbstract) getActivity();
    }

    @Override
    public void hideKeyboard() {

    }

    @Override
    public void displayMessage(String message) {
        getAbstractActivity().displayMessage(message);
    }

    @Override
    public <T> void saveListToPreference(String key, List<T> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);

        getAbstracContext().getSharedPreferences(Constants.SHARE_PREFS, MODE_PRIVATE).edit().putString(key, json).apply();
    }

    @Override
    public <T> List<T> getListFromPreference(String key) {
        Gson gson = new Gson();
        String json = getAbstracContext().getSharedPreferences(Constants.SHARE_PREFS, MODE_PRIVATE).getString(key, null);
        Type type = new TypeToken<List<T>>() {
        }.getType();

        return gson.fromJson(json, type);
    }

    @Override
    public void showToast(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
