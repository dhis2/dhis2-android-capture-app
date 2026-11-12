package org.dhis2.mobile.extension.web.di

import org.koin.core.module.Module

/**
 * The WebView runtime for web extensions. Everything is Android (WebView, SDK access); desktop
 * gets an empty module until it has a web view of its own.
 */
expect val extensionWebModule: Module
