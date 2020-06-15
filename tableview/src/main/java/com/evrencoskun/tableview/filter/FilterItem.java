/*
 * Copyright (c) 2018. Evren Coşkun
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.evrencoskun.tableview.filter;

public class FilterItem {
    private FilterType filterType;
    private String filter;
    private int column;

    public FilterItem(FilterType type, int column, String filter) {
        this.filterType = type;
        this.column = column;
        this.filter = filter;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public String getFilter() {
        return filter;
    }

    public int getColumn() {
        return column;
    }
}