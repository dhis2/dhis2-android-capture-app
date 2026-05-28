package org.dhis2.usescases.searchTrackEntity;

import org.dhis2.mobile.commons.model.MetadataIconData;
import org.dhis2.tracker.search.model.DomainEnrollment;
import org.dhis2.tracker.search.model.TrackedEntitySearchItemAttributeDomain;
import org.dhis2.tracker.search.model.TrackedEntitySearchItemResult;
import org.hisp.dhis.android.core.maintenance.D2ErrorCode;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class SearchTeiModel{

    private LinkedHashMap<String, TrackedEntitySearchItemAttributeDomain> attributeValues;
    private LinkedHashMap<String, TrackedEntitySearchItemAttributeDomain> textAttributeValues;

    private HashMap<String, MetadataIconData> metadataIconDataMap;

    private TrackedEntitySearchItemResult tei;

    private boolean openedAttributeList = false;
    private String sortingKey;
    private String sortingValue;

    @Nullable
    public String onlineErrorMessage;
    @Nullable
    public D2ErrorCode onlineErrorCode;

    public SearchTeiModel() {
        this.tei = null;
        this.attributeValues = new LinkedHashMap<>();
        this.textAttributeValues = new LinkedHashMap<>();
        this.sortingKey = null;
        this.sortingValue = null;
        this.onlineErrorMessage = null;
        this.metadataIconDataMap = new HashMap<>();
    }



    public void addProgramInfo(String programUid,  MetadataIconData metadataIconData) {

            metadataIconDataMap.put(programUid, metadataIconData);

    }


    public LinkedHashMap<String, TrackedEntitySearchItemAttributeDomain> getAttributeValues() {
        return attributeValues;
    }

    public LinkedHashMap<String, TrackedEntitySearchItemAttributeDomain> getTextAttributeValues() {
        return textAttributeValues;
    }

    public void addAttributeValue(String attributeName, TrackedEntitySearchItemAttributeDomain attributeValues) {
        this.attributeValues.put(attributeName, attributeValues);
    }


    public void addTextAttribute(String attributeName, TrackedEntitySearchItemAttributeDomain attributeValue) {
        this.textAttributeValues.put(attributeName, attributeValue);
    }

    public void setAttributeValues(LinkedHashMap<String, TrackedEntitySearchItemAttributeDomain> attributeValues) {
        this.attributeValues = attributeValues;
    }


    public void setTei(TrackedEntitySearchItemResult tei) {
        this.tei = tei;
    }

    public TrackedEntitySearchItemResult getTei() {
        return tei;
    }


    public String getProfilePicturePath() {
        return this.tei.getProfilePicture() != null ? this.tei.getProfilePicture() : "";
    }



    @Nullable
    public String getDefaultTypeIcon() {
        return this.tei.getDefaultTypeIcon();
    }

    @Nullable
    public String getHeader() {
        return this.tei.getHeader();
    }


    public DomainEnrollment getSelectedEnrollment() {
        return this.tei.getSelectedEnrollment();
    }

    public MetadataIconData getMetadataIconData(@Nullable String programUid) {
        MetadataIconData iconData = metadataIconDataMap.get(programUid);
        if (iconData != null) {
            return iconData;
        } else {
            return MetadataIconData.Companion.defaultIcon();
        }
    }

    public Boolean isMetadataIconDataAvailable(@Nullable String programUid) {
        MetadataIconData iconData = metadataIconDataMap.get(programUid);
        if (iconData != null) {
            return !iconData.getIconRes().isEmpty();
        } else {
            return false;
        }
    }

    public void toggleAttributeList() {
        this.openedAttributeList = !this.openedAttributeList;
    }

    public boolean isAttributeListOpen() {
        return this.openedAttributeList;
    }

    public void setSortingValue(kotlin.Pair<String, String> sortingKeyValue) {
        if (sortingKeyValue != null) {
            this.sortingKey = sortingKeyValue.getFirst();
            this.sortingValue = sortingKeyValue.getSecond();
        }
    }

    public String getSortingKey() {
        return sortingKey;
    }

    public String getSortingValue() {
        return sortingValue;
    }


}
