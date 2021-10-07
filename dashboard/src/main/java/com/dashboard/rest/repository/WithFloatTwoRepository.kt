package com.dashboard.rest.repository

import com.dashboard.base.rest.AppBaseRepository
import com.dashboard.controller.ui.business.model.BusinessProfileUpdateRequest
import com.dashboard.rest.TaskCode
import com.dashboard.rest.apiClients.WithFloatsTwoApiClient
import com.dashboard.rest.services.WithFloatTwoRemoteData
import com.dashboard.rest.services.local.DashboardLocalDataSource
import com.framework.base.BaseResponse
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Retrofit

object WithFloatTwoRepository :
  AppBaseRepository<WithFloatTwoRemoteData, DashboardLocalDataSource>() {

  fun uploadBusinessLogo(
    clientId: String?, fpId: String?, reqType: String?, reqId: String?,
    totalChunks: String?, currentChunkNumber: String?, file: RequestBody?
  ): Observable<BaseResponse> {
    return makeRemoteRequest(
      remoteDataSource.uploadBusinessImage(
        clientId = clientId,
        fpId = fpId,
        reqType = reqType,
        reqtId = reqId,
        totalChunks = totalChunks,
        currentChunkNumber = currentChunkNumber,
        file = file
      ), TaskCode.UPLOAD_BUSINESS_IMAGE
    )
  }

  fun updateBusinessProfile(
    profileUpdateRequest: BusinessProfileUpdateRequest
  ): Observable<BaseResponse> {
    return makeRemoteRequest(
      remoteDataSource.updateBusinessProfile(profileUpdateRequest = profileUpdateRequest),
      TaskCode.UPDATE_BUSINESS_PROFILE
    )
  }

  fun postRegisterRIA(
    paramsMap: HashMap<String, String?>
  ):Observable<BaseResponse>{
    return makeRemoteRequest(
      remoteDataSource.postRegisterRIA(hashMap = paramsMap),
      TaskCode.POST_REGISTER_RIA
    )
  }

  override fun getRemoteDataSourceClass(): Class<WithFloatTwoRemoteData> {
    return WithFloatTwoRemoteData::class.java
  }

  override fun getLocalDataSourceInstance(): DashboardLocalDataSource {
    return DashboardLocalDataSource
  }

  override fun getApiClient(): Retrofit {
    return WithFloatsTwoApiClient.shared.retrofit
  }
}
