package org.dhis2.utils;

import android.content.Context;
import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class FilderUtil {

    public static File getFilePath(String value, Context context) {
        File mypath;
        File directory = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), ".dhisFiles");
        if (!directory.mkdirs()) {
            directory.mkdir();
        }
        mypath = new File(directory, value);
        return mypath;
    }

}
