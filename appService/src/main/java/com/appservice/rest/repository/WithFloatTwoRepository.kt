package com.appservice.rest.repository

import com.appservice.base.rest.AppBaseLocalService
import com.appservice.base.rest.AppBaseRepository
import com.appservice.model.serviceProduct.CatalogProduct
import com.appservice.model.serviceProduct.delete.DeleteProductRequest
import com.appservice.model.serviceProduct.update.ProductUpdate
import com.appservice.model.updateBusiness.DeleteBizMessageRequest
import com.appservice.model.updateBusiness.PostUpdateTaskRequest
import com.appservice.rest.TaskCode
import com.appservice.rest.apiClients.WithFloatsApiTwoClient
import com.appservice.rest.services.WithFloatTwoRemoteData
import com.framework.base.BaseResponse
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Retrofit

object WithFloatTwoRepository : AppBaseRepository<WithFloatTwoRemoteData, AppBaseLocalService>() {

  fun createService(request: CatalogProduct?): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.createService(request), TaskCode.POST_CREATE_SERVICE)
  }

  fun updateService(request: ProductUpdate?): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.updateService(request), TaskCode.POST_UPDATE_SERVICE)
  }

  fun deleteService(request: DeleteProductRequest?): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.deleteService(request), TaskCode.POST_UPDATE_SERVICE)
  }

  fun addUpdateImageProductService(
    clientId: String?, requestType: String?, requestId: String?, totalChunks: Int?, currentChunkNumber: Int?,
    productId: String?, requestBody: RequestBody?,
  ): Observable<BaseResponse> {
    return makeRemoteRequest(
      remoteDataSource.addUpdateImageProductService(
        clientId, requestType, requestId, totalChunks,
        currentChunkNumber, productId, requestBody
      ), TaskCode.ADD_UPDATE_IMAGE_PRODUCT_SERVICE
    )
  }

  fun getNotificationCount(clientId: String?, fpId: String?): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.getNotificationCount(clientId, fpId), TaskCode.GET_NOTIFICATION)
  }

  fun getMessageUpdates(map: Map<String?, String?>): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.getMessageUpdates(map), TaskCode.GET_LATEST_UPDATE)
  }

  override fun getRemoteDataSourceClass(): Class<WithFloatTwoRemoteData> {
    return WithFloatTwoRemoteData::class.java
  }

  override fun getLocalDataSourceInstance(): AppBaseLocalService {
    return AppBaseLocalService()
  }

  override fun getApiClient(): Retrofit {
    return WithFloatsApiTwoClient.shared.retrofit
  }

  fun createProduct(request: CatalogProduct?): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.createProduct(request = request), TaskCode.POST_CREATE_PRODUCT)
  }

  fun updateProduct(request: ProductUpdate?): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.updateProduct(request), TaskCode.POST_UPDATE_PRODUCT)
  }

  fun deleteProduct(request: DeleteProductRequest?): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.deleteProduct(request), TaskCode.POST_UPDATE_PRODUCT)
  }

  fun addUpdateImageProduct(
    clientId: String?, requestType: String?, requestId: String?, totalChunks: Int?, currentChunkNumber: Int?,
    productId: String?, requestBody: RequestBody?,
  ): Observable<BaseResponse> {
    return makeRemoteRequest(
      remoteDataSource.addUpdateImageProduct(
        clientId, requestType, requestId, totalChunks,
        currentChunkNumber, productId, requestBody
      ), TaskCode.ADD_UPDATE_IMAGE_PRODUCT_SERVICE
    )
  }

  fun putBizMessageUpdate(request: PostUpdateTaskRequest?): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.putBizMessageUpdate(request), TaskCode.PUT_BIZ_MESSAGE_UPDATE)
  }

  fun getBizWebMessage( id: String?,clientId: String?): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.getBizWebMessage(id,clientId), TaskCode.GET_BIZ_MESSAGE_WEB)
  }
  fun deleteBizMessageUpdate(request: DeleteBizMessageRequest?): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.deleteBizMessageUpdate(request), TaskCode.DELETE_BIZ_MESSAGE_UPDATE)
  }

  fun putBizImageUpdate(
    clientId: String?, requestType: String?, requestId: String?, totalChunks: Int?, currentChunkNumber: Int?,
    socialParmeters: String?, bizMessageId: String?, sendToSubscribers: Boolean?, requestBody: RequestBody?,
  ): Observable<BaseResponse> {
    return makeRemoteRequest(
      remoteDataSource.putBizImageUpdate(
        clientId, requestType, requestId, totalChunks, currentChunkNumber,
        socialParmeters, bizMessageId, sendToSubscribers, requestBody
      ), TaskCode.PUT_IMAGE_BIZ_UPDATE
    )
  }
}
