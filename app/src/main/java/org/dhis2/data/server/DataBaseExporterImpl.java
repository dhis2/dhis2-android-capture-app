package org.dhis2.data.server;

import android.database.Cursor;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.dhis2.utils.FileResourcesUtil;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.BaseDataModel;
import org.hisp.dhis.android.core.D2Manager;
import org.hisp.dhis.android.core.common.BaseDataObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Flowable;
import timber.log.Timber;

public class DataBaseExporterImpl implements DataBaseExporter {

    private final D2 d2;

    public DataBaseExporterImpl(D2 d2) {
        this.d2 = d2;
    }

    @Override
    public Flowable<Boolean> exportDb() {
        JSONObject dataJson = new JSONObject();
        return Flowable.just(dataJson)
                .flatMap(jsonObject ->
                        Flowable.fromCallable(() -> {
                            Cursor cursor = D2Manager.getD2().databaseAdapter().database().rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
                            cursor.moveToFirst();
                            List<String> tableNames = new ArrayList<>();
                            while (!cursor.isAfterLast()) {
                                tableNames.add(cursor.getString(0));
                                cursor.moveToNext();
                            }
                            cursor.close();
                            return tableNames;
                        })
                                .flatMapIterable(tableNames -> tableNames)
                                .map(table -> {
                                    Cursor cursor = D2Manager.getD2().databaseAdapter().database().rawQuery("SELECT * FROM " + table, null);
                                    cursor.moveToFirst();
                                    JSONArray result = new JSONArray();
                                    while (!cursor.isAfterLast()) {
                                        int totalColumn = cursor.getColumnCount();
                                        JSONObject rowObject = new JSONObject();

                                        for (int i = 0; i < totalColumn; i++) {
                                            if (cursor.getColumnName(i) != null) {
                                                try {
                                                    rowObject.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                                                } catch (Exception e) {
                                                    Timber.d(e);
                                                }
                                            }
                                        }
                                        result.put(rowObject);
                                        cursor.moveToNext();
                                    }
                                    cursor.close();
                                    jsonObject.put(table, result);
                                    return table;
                                }).toList().toFlowable()
                )
                .map(data -> FileResourcesUtil.writeToFile(dataJson.toString(), null));
    }

    @Override
    public Flowable<Boolean> importDb() {
        return Flowable.fromCallable(FileResourcesUtil::readFromFile)
                .map(JSONObject::new)
                .map(dataJson -> {
                    Iterator<String> it = dataJson.keys();
                    while (it.hasNext()) {
                        String table = it.next();
                        JSONArray array = dataJson.getJSONArray(table);
                        Iterator<String> columns = array.getJSONObject(0).keys();
                        //TODO: SAVE EVERYTHING IN THE DATABASE
                        new TypeToken<List<? extends BaseDataObject>>(){}.getType();
                        Class<? extends BaseDataObject> klass = Class.forName(table).asSubclass(BaseDataObject.class);
                        List<BaseDataObject> dataList = new Gson().fromJson(array.toString(),TypeToken.of(klass).getType());
                    }
                    return true;
                });
    }
}
