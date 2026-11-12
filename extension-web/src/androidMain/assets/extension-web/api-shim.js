/*
 * Injected at document start into every frame of the plugin WebView (the bridge page and the
 * plugin iframe), before any plugin script runs.
 *
 * DHIS2 web plugins reach the server through @dhis2/app-runtime, which calls window.fetch. This
 * routes every same-origin request below /api/ to the app over the `dhis2Native` message bridge,
 * where the SDK answers from the local database, so the plugin works offline unchanged. The request
 * body travels with it, which the WebView's own request interception cannot see.
 */
(function () {
    'use strict';
    if (window.__dhis2ApiShim) return;
    window.__dhis2ApiShim = true;

    var bridge = window.dhis2Native;
    if (!bridge) {
        console.error('[dhis2] native bridge missing; API calls will fail');
        return;
    }

    var pending = {};
    var nextId = 1;

    bridge.addEventListener('message', function (event) {
        var message;
        try {
            message = JSON.parse(event.data);
        } catch (e) {
            return;
        }
        if (message.type !== 'api' || !pending[message.id]) return;
        var resolve = pending[message.id];
        delete pending[message.id];
        resolve(message);
    });

    function apiPath(url) {
        var parsed;
        try {
            parsed = new URL(url, window.location.href);
        } catch (e) {
            return null;
        }
        if (parsed.origin !== window.location.origin) return null;
        var index = parsed.pathname.indexOf('/api/');
        return index < 0 ? null : parsed.pathname.substring(index) + parsed.search;
    }

    function callNative(method, path, body) {
        return new Promise(function (resolve) {
            var id = nextId++;
            pending[id] = resolve;
            bridge.postMessage(JSON.stringify({
                type: 'api', id: id, method: method, url: path, body: body,
            }));
        });
    }

    function bodyText(body) {
        if (body === undefined || body === null) return Promise.resolve(null);
        if (typeof body === 'string') return Promise.resolve(body);
        return new Response(body).text();
    }

    var originalFetch = window.fetch;
    window.fetch = function (input, init) {
        var request = input instanceof Request ? input : null;
        var path = apiPath(request ? request.url : String(input));
        if (!path) return originalFetch.apply(this, arguments);

        var method = ((init && init.method) || (request && request.method) || 'GET').toUpperCase();
        var body = init && init.body !== undefined
            ? bodyText(init.body)
            : (request && method !== 'GET' && method !== 'HEAD' ? request.text() : Promise.resolve(null));

        return body
            .then(function (text) { return callNative(method, path, text); })
            .then(function (reply) {
                return new Response(reply.body, {
                    status: reply.status,
                    headers: { 'Content-Type': reply.contentType },
                });
            });
    };

    // Some libraries still use XMLHttpRequest; route those the same way.
    var OriginalXHR = window.XMLHttpRequest;
    function BridgedXHR() {
        var xhr = new OriginalXHR();
        var method = 'GET';
        var path = null;
        var self = this;
        var listeners = {};
        this.readyState = 0;
        this.status = 0;
        this.responseText = '';
        this.response = '';
        this.withCredentials = false;
        this.open = function (m, url) {
            method = (m || 'GET').toUpperCase();
            path = apiPath(url);
            if (!path) return xhr.open.apply(xhr, arguments);
            self.readyState = 1;
        };
        this.setRequestHeader = function () {
            if (!path) xhr.setRequestHeader.apply(xhr, arguments);
        };
        this.getResponseHeader = function (name) {
            if (!path) return xhr.getResponseHeader(name);
            return name && name.toLowerCase() === 'content-type' ? self._contentType : null;
        };
        this.getAllResponseHeaders = function () {
            return path ? 'content-type: ' + self._contentType + '\r\n' : xhr.getAllResponseHeaders();
        };
        this.addEventListener = function (type, fn) {
            if (!path) return xhr.addEventListener(type, fn);
            (listeners[type] = listeners[type] || []).push(fn);
        };
        this.abort = function () { if (!path) xhr.abort(); };
        function fire(type) {
            var handler = self['on' + type];
            if (handler) handler.call(self, { type: type, target: self });
            (listeners[type] || []).forEach(function (fn) { fn.call(self, { type: type, target: self }); });
        }
        this.send = function (body) {
            if (!path) {
                ['onload', 'onerror', 'onreadystatechange'].forEach(function (key) {
                    if (self[key]) xhr[key] = function (e) {
                        self.readyState = xhr.readyState;
                        self.status = xhr.status;
                        self.responseText = xhr.responseText;
                        self.response = xhr.response;
                        self[key].call(self, e);
                    };
                });
                return xhr.send(body);
            }
            bodyText(body)
                .then(function (text) { return callNative(method, path, text); })
                .then(function (reply) {
                    self.readyState = 4;
                    self.status = reply.status;
                    self._contentType = reply.contentType;
                    self.responseText = reply.body;
                    self.response = reply.body;
                    fire('readystatechange');
                    fire('load');
                    fire('loadend');
                });
        };
    }
    window.XMLHttpRequest = BridgedXHR;

    // A plugin's service worker would fetch outside this shim, and outside the app's request
    // interception, so every plugin runs without one.
    if (navigator.serviceWorker) {
        try {
            navigator.serviceWorker.register = function () {
                return Promise.reject(new Error('Service workers are disabled for DHIS2 Android plugins'));
            };
        } catch (e) {
            console.warn('[dhis2] could not disable service worker registration', e);
        }
    }
})();
