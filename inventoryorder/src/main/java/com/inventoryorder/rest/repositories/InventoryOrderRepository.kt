package com.inventoryorder.rest.repositories

import com.framework.base.BaseResponse
import com.inventoryorder.base.rest.AppBaseLocalService
import com.inventoryorder.base.rest.AppBaseRepository
import com.inventoryorder.model.orderfilter.OrderFilterRequest
import com.inventoryorder.model.ordersummary.OrderSummaryRequest
import com.inventoryorder.rest.TaskCode
import com.inventoryorder.rest.apiClients.AssuredPurchaseClient
import com.inventoryorder.rest.services.InventoryOrderRemoteDataSource
import io.reactivex.Observable
import retrofit2.Retrofit

object InventoryOrderRepository : AppBaseRepository<InventoryOrderRemoteDataSource, AppBaseLocalService>() {

  override fun getRemoteDataSourceClass(): Class<InventoryOrderRemoteDataSource> {
    return InventoryOrderRemoteDataSource::class.java
  }

  override fun getLocalDataSourceInstance(): AppBaseLocalService {
    return AppBaseLocalService()
  }

  fun getSellerSummary(clientId: String?, sellerId: String?): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.getSellerSummary(clientId, sellerId), TaskCode.GET_SELLER_SUMMARY)
  }

  fun getSellerOrders(auth: String, request: OrderSummaryRequest): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.getSellerOrders(auth, request.clientId, request.sellerId, request.orderMode, request.deliveryMode, request.orderStatus, request.paymentStatus, request.skip, request.limit), TaskCode.GET_LIST_ORDER)
  }

  fun getSellerOrdersFilter(auth: String, request: OrderFilterRequest): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.getSellerOrdersFiler(auth, request.clientId, request), TaskCode.GET_LIST_ORDER_FILTER)
  }

  fun getAssurePurchaseOrders(request: OrderSummaryRequest): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.getAssurePurchaseOrders(request.clientId, request.sellerId, request.skip, request.limit), TaskCode.GET_ASSURE_PURCHASE_ORDER)
  }

  fun getCancelledOrders(request: OrderSummaryRequest): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.getCancelledOrders(request.clientId, request.sellerId, request.skip, request.limit), TaskCode.GET_LIST_CANCELLED_ORDER)
  }

  fun getOrderDetails(clientId: String?, orderId: String?): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.getOrderDetails(clientId, orderId), TaskCode.GET_ORDER_DETAILS)
  }

  fun getInCompleteOrders(request: OrderSummaryRequest): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.getInCompleteOrders(request.clientId, request.sellerId, request.skip, request.limit), TaskCode.GET_LIST_IN_COMPLETE_ORDER)
  }

  fun confirmOrder(clientId: String?, orderId: String?): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.confirmOrder(clientId, orderId), TaskCode.CONFIRM_ORDER_TASK)
  }


  fun sendPaymentReminder(clientId: String?, orderId: String?): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.sendPaymentReminder(clientId, orderId), TaskCode.SEND_LINK_ORDER_TASK)
  }

  fun cancelOrder(clientId: String?, orderId: String?, cancellingEntity: String?): Observable<BaseResponse> {
    return makeRemoteRequest(remoteDataSource.cancelOrder(clientId, orderId, cancellingEntity), TaskCode.CANCEL_ORDER_TASK)
  }

  override fun getApiClient(): Retrofit {
    return AssuredPurchaseClient.shared.retrofit
  }
}