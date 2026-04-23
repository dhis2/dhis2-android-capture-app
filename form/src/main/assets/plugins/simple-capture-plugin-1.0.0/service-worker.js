// A simple, no-op service worker that takes immediate control and tears
// everything down; has no fetch handler. Fixes apps with rogue service workers
// and gets overrwritten in apps using PWA
self.addEventListener('install', () => {
    self.skipWaiting()
})

self.addEventListener('activate', async () => {
    console.log('Removing previous service worker')
    // Unregister, in case app doesn't
    self.registration.unregister()
    // Delete all caches
    const keys = await self.caches.keys()
    await Promise.all(keys.map((key) => self.caches.delete(key)))
    // Force refresh all windows
    const clients = await self.clients.matchAll({ type: 'window' })
    clients.forEach((client) => client.navigate(client.url))
})
