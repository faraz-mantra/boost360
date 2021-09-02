package com.framework.rest

import com.framework.BaseApplication
import com.framework.pref.UserSessionManager
import com.framework.pref.getAccessTokenAuth
import okhttp3.Interceptor
import okhttp3.Response

class ServiceInterceptor(var isAuthRemove: Boolean, var token: String?) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    var request = chain.request()
    val session = UserSessionManager(BaseApplication.instance)
    val tokenResult = session.getAccessTokenAuth()
    if (isAuthRemove.not() && tokenResult?.token.isNullOrEmpty().not()) {
      request = request.newBuilder().addHeader("Authorization", "Bearer ${tokenResult?.token}").build()
    }
    return chain.proceed(request)
  }
}