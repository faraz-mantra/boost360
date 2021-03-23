package dev.patrickgold.florisboard.customization.network

import dev.patrickgold.florisboard.customization.model.response.CustomerDetails
import dev.patrickgold.florisboard.customization.model.response.Product
import dev.patrickgold.florisboard.customization.model.response.Updates
import retrofit2.Retrofit
import java.util.*

object BusinessFeatureRepository : AppBaseRepository<BusinessFeaturesRemoteData, AppBaseLocalService>() {

    suspend fun getAllUpdates(fpId: String, clientId: String, skipBy: Int, limit: Int): Updates {
        val queries: MutableMap<String, String> = HashMap()
        queries["fpId"] = fpId
        queries["clientId"] = clientId
        queries["skipBy"] = skipBy.toString() + ""
        queries["limit"] = limit.toString() + ""
        return remoteDataSource.getAllUpdates(queries)
    }

    suspend fun getAllDetails(fpTag: String, clientId: String): CustomerDetails {
        val queries: MutableMap<String, String> = HashMap()
        queries["clientId"] = clientId
        return remoteDataSource.getAllDetails(fpTag, queries)
    }

    suspend fun getAllProducts(fpTag: String, clientId: String, skipBy: Int, identifierType: String): List<Product> {
        val queries: MutableMap<String, String> = HashMap()
        queries["fpTag"] = fpTag
        queries["clientId"] = clientId
        queries["skipBy"] = skipBy.toString() + ""
        queries["identifierType"] = identifierType
        return remoteDataSource.getAllProducts(queries)
    }

    fun getAllImageList(listener: GetGalleryImagesAsyncTask.GetGalleryImagesInterface, fpId: String) {
        val gallery = GetGalleryImagesAsyncTask()
        gallery.setGalleryInterfaceListener(listener, fpId)
        gallery.execute()
    }


    override fun getRemoteDataSourceClass(): Class<BusinessFeaturesRemoteData> {
        return BusinessFeaturesRemoteData::class.java
    }

    override fun getLocalDataSourceInstance(): AppBaseLocalService = AppBaseLocalService()

    override fun getApiClient(): Retrofit = BusinessFeatureApiClient.shared.retrofit
}
