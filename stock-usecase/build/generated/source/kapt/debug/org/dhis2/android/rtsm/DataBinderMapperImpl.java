package org.dhis2.android.rtsm;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import androidx.databinding.DataBinderMapper;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Generated;
import org.dhis2.android.rtsm.databinding.ActivityScannerBindingImpl;

@Generated("Android Data Binding")
public class DataBinderMapperImpl extends DataBinderMapper {
  private static final int LAYOUT_ACTIVITYSCANNER = 1;

  private static final SparseIntArray INTERNAL_LAYOUT_ID_LOOKUP = new SparseIntArray(1);

  static {
    INTERNAL_LAYOUT_ID_LOOKUP.put(org.dhis2.android.rtsm.R.layout.activity_scanner, LAYOUT_ACTIVITYSCANNER);
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View view, int layoutId) {
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = view.getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
        case  LAYOUT_ACTIVITYSCANNER: {
          if ("layout/activity_scanner_0".equals(tag)) {
            return new ActivityScannerBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_scanner is invalid. Received: " + tag);
        }
      }
    }
    return null;
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View[] views, int layoutId) {
    if(views == null || views.length == 0) {
      return null;
    }
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = views[0].getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
      }
    }
    return null;
  }

  @Override
  public int getLayoutId(String tag) {
    if (tag == null) {
      return 0;
    }
    Integer tmpVal = InnerLayoutIdLookup.sKeys.get(tag);
    return tmpVal == null ? 0 : tmpVal;
  }

  @Override
  public String convertBrIdToString(int localId) {
    String tmpVal = InnerBrLookup.sKeys.get(localId);
    return tmpVal;
  }

  @Override
  public List<DataBinderMapper> collectDependencies() {
    ArrayList<DataBinderMapper> result = new ArrayList<DataBinderMapper>(2);
    result.add(new androidx.databinding.library.baseAdapters.DataBinderMapperImpl());
    result.add(new org.dhis2.commons.DataBinderMapperImpl());
    return result;
  }

  private static class InnerBrLookup {
    static final SparseArray<String> sKeys = new SparseArray<String>(17);

    static {
      sKeys.put(0, "_all");
      sKeys.put(1, "featureState");
      sKeys.put(2, "filterCount");
      sKeys.put(3, "filterItem");
      sKeys.put(4, "filterType");
      sKeys.put(5, "item");
      sKeys.put(6, "message");
      sKeys.put(7, "name");
      sKeys.put(8, "negativeText");
      sKeys.put(9, "ou");
      sKeys.put(10, "positiveText");
      sKeys.put(11, "programType");
      sKeys.put(12, "title");
      sKeys.put(13, "value");
      sKeys.put(14, "viewModel");
      sKeys.put(15, "workingList");
      sKeys.put(16, "workingListScope");
    }
  }

  private static class InnerLayoutIdLookup {
    static final HashMap<String, Integer> sKeys = new HashMap<String, Integer>(1);

    static {
      sKeys.put("layout/activity_scanner_0", org.dhis2.android.rtsm.R.layout.activity_scanner);
    }
  }
}
