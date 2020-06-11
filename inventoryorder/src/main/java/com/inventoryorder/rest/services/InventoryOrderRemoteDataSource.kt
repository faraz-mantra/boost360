package com.inventoryorder.rest.services

import com.inventoryorder.model.OrderConfirmStatus
import com.inventoryorder.model.orderfilter.OrderFilterRequest
import com.inventoryorder.rest.EndPoints
import com.inventoryorder.rest.response.OrderSummaryResponse
import com.inventoryorder.rest.response.order.InventoryOrderListResponse
import com.inventoryorder.rest.response.order.OrderDetailResponse
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

interface InventoryOrderRemoteDataSource {

  @GET(EndPoints.GET_SELLER_SUMMARY_URL)
  fun getSellerSummary(@Query("clientId") clientId: String?,
                       @Query("sellerId") sellerId: String?): Observable<Response<OrderSummaryResponse>>

  @GET(EndPoints.GET_LIST_ORDER_URL)
  fun getSellerOrders(@Header("Authorization") auth: String,
                      @Query("clientId") clientId: String?,
                      @Query("sellerId") sellerId: String?,
                      @Query("orderMode") orderMode: String?,
                      @Query("deliveryMode") deliveryMode: String?,
                      @Query("orderStatus") orderStatus: String?,
                      @Query("paymentStatus") paymentStatus: String?,
                      @Query("skip") skip: Int?,
                      @Query("limit") limit: Int?): Observable<Response<InventoryOrderListResponse>>

  @POST(EndPoints.GET_LIST_ORDER_FILTER_URL)
  fun getSellerOrdersFiler(@Header("Authorization") auth: String,
                           @Query("clientId") clientId: String?,
                           @Body request: OrderFilterRequest): Observable<Response<InventoryOrderListResponse>>

  @GET(EndPoints.GET_LIST_ASSURE_PURCHASE_ORDER)
  fun getAssurePurchaseOrders(@Query("clientId") clientId: String?,
                              @Query("sellerId") sellerId: String?,
                              @Query("skip") skip: Int?,
                              @Query("limit") limit: Int?): Observable<Response<InventoryOrderListResponse>>

  @GET(EndPoints.GET_LIST_CANCELLED_ORDER)
  fun getCancelledOrders(@Query("clientId") clientId: String?,
                         @Query("sellerId") sellerId: String?,
                         @Query("skip") skip: Int?,
                         @Query("limit") limit: Int?): Observable<Response<InventoryOrderListResponse>>

  @GET(EndPoints.GET_ORDER_DETAIL)
  fun getOrderDetails(@Query("clientId") clientId: String?,
                      @Query("orderId") orderId: String?): Observable<Response<OrderDetailResponse>>

  @GET(EndPoints.GET_LIST_IN_COMPLETE_ORDER)
  fun getInCompleteOrders(@Query("clientId") clientId: String?,
                          @Query("sellerId") sellerId: String?,
                          @Query("skip") skip: Int?,
                          @Query("limit") limit: Int?): Observable<Response<InventoryOrderListResponse>>


  @GET(EndPoints.GET_CONFIRM_ORDER)
  fun confirmOrder(@Query("clientId") clientId: String?,
                   @Query("orderId") orderId: String?): Observable<Response<OrderConfirmStatus>>

  @GET(EndPoints.GET_CANCEL_ORDER)
  fun cancelOrder(@Query("clientId") clientId: String?,
                  @Query("orderId") orderId: String?,
                  @Query("cancellingEntity") cancellingEntity: String?): Observable<Response<OrderConfirmStatus>>
}