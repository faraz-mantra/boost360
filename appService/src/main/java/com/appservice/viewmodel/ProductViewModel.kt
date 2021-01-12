package com.appservice.viewmodel

import androidx.lifecycle.LiveData
import com.appservice.model.serviceProduct.Product
import com.appservice.model.serviceProduct.addProductImage.ProductImageRequest
import com.appservice.model.serviceProduct.addProductImage.deleteRequest.ProductImageDeleteRequest
import com.appservice.model.serviceProduct.delete.DeleteProductRequest
import com.appservice.model.serviceProduct.gstProduct.ProductGstDetailRequest
import com.appservice.model.serviceProduct.gstProduct.update.ProductUpdateRequest
import com.appservice.model.serviceProduct.update.ProductUpdate
import com.appservice.rest.repository.AssuredWithFloatRepository
import com.appservice.rest.repository.KitWebActionRepository
import com.appservice.rest.repository.WithFloatRepository
import com.appservice.rest.repository.WithFloatTwoRepository
import com.framework.base.BaseResponse
import com.framework.models.BaseViewModel
import com.framework.models.toLiveData
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProductViewModel :BaseViewModel() {
    fun createProduct(request: Product?): LiveData<BaseResponse> {
        return WithFloatTwoRepository.createService(request).toLiveData()
    }

    fun updateProduct(request: ProductUpdate?): LiveData<BaseResponse> {
        return WithFloatTwoRepository.updateService(request).toLiveData()
    }

    fun deleteService(request: DeleteProductRequest?): LiveData<BaseResponse> {
        return WithFloatTwoRepository.deleteService(request).toLiveData()
    }

    fun addUpdateImageProductService(clientId: String?, requestType: String?, requestId: String?, totalChunks: Int?, currentChunkNumber: Int?,
                                     productId: String?, requestBody: RequestBody?): LiveData<BaseResponse> {
        return WithFloatTwoRepository.addUpdateImageProductService(clientId, requestType, requestId, totalChunks,
                currentChunkNumber, productId, requestBody).toLiveData()
    }

    fun addProductGstDetail(auth: String?, request: ProductGstDetailRequest?): LiveData<BaseResponse> {
        return KitWebActionRepository.addProductGstDetail(auth, request).toLiveData()
    }

    fun updateProductGstDetail(auth: String?, request: ProductUpdateRequest?): LiveData<BaseResponse> {
        return KitWebActionRepository.updateProductGstDetail(auth, request).toLiveData()
    }

    fun getProductGstDetail(auth: String?, query: String?): LiveData<BaseResponse> {
        return KitWebActionRepository.getProductGstDetail(auth, query).toLiveData()
    }

    fun uploadImageProfile(auth: String?, assetFileName: String?, file: MultipartBody.Part?): LiveData<BaseResponse> {
        return KitWebActionRepository.uploadImageProfile(auth, assetFileName, file).toLiveData()
    }

    fun addProductImage(auth: String?, request: ProductImageRequest?): LiveData<BaseResponse> {
        return KitWebActionRepository.addProductImage(auth, request).toLiveData()
    }

    fun deleteProductImage(auth: String?, request: ProductImageDeleteRequest?): LiveData<BaseResponse> {
        return KitWebActionRepository.deleteProductImage(auth, request).toLiveData()
    }

    fun getProductImage(auth: String?, query: String?): LiveData<BaseResponse> {
        return KitWebActionRepository.getProductImage(auth, query).toLiveData()
    }

    fun getPickUpAddress(fpId: String?): LiveData<BaseResponse> {
        return AssuredWithFloatRepository.getPickUpAddress(fpId).toLiveData()
    }

    fun userAccountDetails(fpId: String?, clientId: String?): LiveData<BaseResponse> {
        return WithFloatRepository.userAccountDetail(fpId, clientId).toLiveData()
    }
}