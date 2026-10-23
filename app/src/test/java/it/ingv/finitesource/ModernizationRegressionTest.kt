package it.ingv.finitesource

import android.Manifest
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.core.os.BundleCompat
import androidx.test.core.app.ApplicationProvider
import it.ingv.finitesource.data.local.Products
import it.ingv.finitesource.data.local.database.Converters
import it.ingv.finitesource.ui.persistentbottomsheet.behavior.ViewPagerBottomSheetBehavior
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import android.view.View
import org.openapitools.client.apis.DefaultApi
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.infrastructure.Serializer
import org.openapitools.client.models.EventMetadata
import it.ingv.finitesource.data.local.earthquake.focalplane.geojson.CustomGeoJson
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import java.io.File
import java.io.IOException
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 28, 36], application = android.app.Application::class)
class ModernizationRegressionTest {
    @Test
    fun sharedEventTextStaysIdenticalInBothLanguages() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val arguments = arrayOf<Any>("Example", 6.5, "date UTC", 10.5, "http://terremoti.ingv.it/event/1")
        for ((locale, format) in listOf(
            Locale.ENGLISH to "Earthquake: %s%nMagnitude: %.1f%nDate: %s%nDepth: %.1f km%nINGV URL: %s",
            Locale.ITALIAN to "Terremoto: %s%nMagnitudo: %.1f%nData: %s%nProfondità: %.1f km%nLink INGV: %s%n"
        )) {
            val config = android.content.res.Configuration(context.resources.configuration).apply { setLocale(locale) }
            val localizedContext = context.createConfigurationContext(config)
            assertEquals(String.format(locale, format, *arguments), localizedContext.getString(R.string.share_event_format, *arguments))
        }
    }

    @Test
    fun generatedAndKspMoshiAdaptersKeepJsonFieldNames() {
        val metadata = Serializer.moshiBuilder.build().adapter(EventMetadata::class.java)
            .fromJson("""{"event_count":0,"events":[]}""")!!
        assertEquals(0L, metadata.eventCount)
        assertTrue(metadata.events.isEmpty())
        val geoJson = CustomGeoJson.parseString("""{
            "type":"FeatureCollection","features":[{
                "type":"Feature","geometry":{"type":"Polygon","coordinates":[[[1.0,2.0],[3.0,4.0],[1.0,2.0]]]},
                "properties":{"Rake_d":90.0,"Slip_m":2.5}
            }]
        }""")
        assertEquals(2.5, geoJson.maxSlip, 0.0)
        assertEquals(90.0, geoJson.features.single().properties.rakeD!!, 0.0)
        assertEquals(geoJson, CustomGeoJson.parseString(geoJson.stringify()))
    }

    @Test
    fun generatedApiPreservesEndpointAndDefaultQuery() {
        val api = ApiClient().createService(DefaultApi::class.java)
        assertEquals("https://finitesource.ingv.it/v3/query?id=201504250611_01&details=true",
            api.queryGet(id = "201504250611_01").request().url.toString())
        assertEquals("https://finitesource.ingv.it/v3/query?details=false",
            api.queryGet(details = false).request().url.toString())
    }

    @Test
    fun bottomSheetStillAcceptsOnlyTouchVerticalScrolls() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val parent = CoordinatorLayout(context)
        val child = View(context)
        val behavior = ViewPagerBottomSheetBehavior<View>()
        assertTrue(behavior.onStartNestedScroll(parent, child, child, child, ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH))
        assertFalse(behavior.onStartNestedScroll(parent, child, child, child, ViewCompat.SCROLL_AXIS_HORIZONTAL, ViewCompat.TYPE_TOUCH))
        assertFalse(behavior.onStartNestedScroll(parent, child, child, child, ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH))
    }

    @Test
    fun gallerySavePublishesJpegAndPreservesMetadata() {
        val provider = Robolectric.buildContentProvider(GalleryProvider::class.java).create("media").get()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)

        assertEquals("content://media/external/images/media/1", saveImage(context, bitmap, true))
        assertEquals("image/jpeg", provider.inserted.getAsString(MediaStore.Images.Media.MIME_TYPE))
        assertEquals("image", provider.inserted.getAsString(MediaStore.Images.Media.DESCRIPTION))
        assertTrue(provider.inserted.getAsString(MediaStore.Images.Media.DISPLAY_NAME).endsWith(".jpg"))
        assertTrue(provider.output.length() > 0)
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            assertEquals(1, provider.inserted.getAsInteger(MediaStore.Images.Media.IS_PENDING))
            assertEquals(0, provider.published.getAsInteger(MediaStore.Images.Media.IS_PENDING))
        } else {
            assertFalse(provider.inserted.containsKey(MediaStore.Images.Media.IS_PENDING))
        }
        assertEquals(context.getString(R.string.image_saved), ShadowToast.getTextOfLatestToast())
        assertFalse(provider.deleted)
    }

    @Test
    fun failedWriteDeletesPartialImageAndCancelsShare() {
        val provider = Robolectric.buildContentProvider(GalleryProvider::class.java).create("media").get()
        provider.failWrite = true
        val context = ApplicationProvider.getApplicationContext<Context>()

        shareImage(context, Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888))

        assertTrue(provider.deleted)
        assertEquals(context.getString(R.string.image_save_failed), ShadowToast.getTextOfLatestToast())
        assertNull(shadowOf(ApplicationProvider.getApplicationContext<android.app.Application>()).nextStartedActivity)
    }

    @Test
    fun failedInsertReturnsNullWithoutSuccessToast() {
        val provider = Robolectric.buildContentProvider(GalleryProvider::class.java).create("media").get()
        provider.failInsert = true
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertNull(saveImage(context, Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888), true))
        assertFalse(provider.deleted)
        assertEquals(context.getString(R.string.image_save_failed), ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun productTabsAndStoredProductEncodingStayCompatible() {
        val products = listOf(Products.FINITE_SOURCE, Products.SCENARIOS, Products.FOOTPRINTS)
        val encoded = "\"finite_source\"\n\"scenarios\"\n\"footprints\"\n"
        assertEquals(products, Products.entries.toList())
        assertEquals(encoded, Converters().productListToString(products))
        assertEquals(products, Converters().stringToProductList(encoded))
        for (product in products) {
            val fragment = product.newFragmentInstance()
            val arguments = Bundle(fragment.arguments!!)
            assertEquals(product, BundleCompat.getParcelable(arguments, "product", Products::class.java))
        }
    }

    @Test
    @Config(sdk = [28])
    fun permissionGrantRetriesOnlyOriginalAction() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val activity = controller.get()
        shadowOf(activity).denyPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        var actions = 0
        withGalleryPermission(activity) { actions++ }
        val request = shadowOf(activity).lastRequestedPermission!!
        withGalleryPermission(activity) { actions += 100 }
        assertEquals(0, actions)
        activity.activityResultRegistry.dispatchResult(request.requestCode, true)
        assertEquals(1, actions)
        controller.pause().stop().destroy()
    }

    @Test
    @Config(sdk = [28])
    fun alreadyGrantedStoragePermissionNeedsNoPrompt() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        shadowOf(controller.get()).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        var actions = 0
        withGalleryPermission(controller.get()) { actions++ }
        assertEquals(1, actions)
        assertNull(shadowOf(controller.get()).lastRequestedPermission)
        controller.pause().stop().destroy()
    }

    @Test
    @Config(sdk = [28])
    fun permissionDenialAndDestroyedActivityCancelAction() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val activity = controller.get()
        shadowOf(activity).denyPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        var actions = 0
        withGalleryPermission(activity) { actions++ }
        activity.activityResultRegistry.dispatchResult(shadowOf(activity).lastRequestedPermission!!.requestCode, false)
        assertEquals(0, actions)
        assertEquals(activity.getString(R.string.image_save_failed), ShadowToast.getTextOfLatestToast())
        withGalleryPermission(activity) { actions++ }
        val request = shadowOf(activity).lastRequestedPermission!!
        controller.pause().stop().destroy()
        activity.activityResultRegistry.dispatchResult(request.requestCode, true)
        assertEquals(0, actions)
    }

    @Test
    @Config(sdk = [36])
    fun scopedStorageNeedsNoPermissionPrompt() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        var actions = 0
        withGalleryPermission(controller.get()) { actions++ }
        assertEquals(1, actions)
        assertNull(shadowOf(controller.get()).lastRequestedPermission)
        controller.pause().stop().destroy()
    }

    class GalleryProvider : ContentProvider() {
        var inserted = ContentValues()
        var published = ContentValues()
        var deleted = false
        var failWrite = false
        var failInsert = false
        lateinit var output: File

        override fun onCreate(): Boolean {
            output = File.createTempFile("gallery-test", ".jpg", context!!.cacheDir)
            return true
        }

        override fun insert(uri: Uri, values: ContentValues?): Uri? {
            inserted = ContentValues(values)
            return if (failInsert) null else Uri.parse("content://media/external/images/media/1")
        }

        override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
            if (failWrite) throw IOException("Storage unavailable")
            return ParcelFileDescriptor.open(output, ParcelFileDescriptor.MODE_READ_WRITE)
        }

        override fun update(uri: Uri, values: ContentValues?, selection: String?, args: Array<out String>?): Int {
            published = ContentValues(values)
            return 1
        }

        override fun delete(uri: Uri, selection: String?, args: Array<out String>?): Int {
            deleted = true
            output.delete()
            return 1
        }

        override fun query(uri: Uri, projection: Array<out String>?, selection: String?, args: Array<out String>?, sort: String?): Cursor? = null
        override fun getType(uri: Uri): String = "image/jpeg"
    }
}
