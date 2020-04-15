package com.boost.upgrades.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.biz2.nowfloats.boost.updates.data.remote.ApiInterface
import com.boost.upgrades.data.api_model.GetPurchaseOrder.GetPurchaseOrderResponse
import com.boost.upgrades.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject

class HistoryViewModel : ViewModel() {
    private var purchaseResult: MutableLiveData<GetPurchaseOrderResponse> = MutableLiveData()
    private var _upiPayment: MutableLiveData<JSONObject> = MutableLiveData()
    private var _cardData: MutableLiveData<JSONObject> = MutableLiveData()
    private var _netBankingData: MutableLiveData<JSONObject> = MutableLiveData()


    var updatesError: MutableLiveData<String> = MutableLiveData()
    var updatesLoader: MutableLiveData<Boolean> = MutableLiveData()

    val compositeDisposable = CompositeDisposable()
    var ApiService = Utils.getRetrofit().create(ApiInterface::class.java)


    fun updatesLoader(): LiveData<Boolean> {
        return updatesLoader
    }

    fun updateResult(): LiveData<GetPurchaseOrderResponse>{
        return purchaseResult
    }

    fun loadPurchasedItems(fpid: String, clientId: String) {
        updatesLoader.postValue(true)
        compositeDisposable.add(
                ApiService.getPurchasedOrders(fpid, clientId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    purchaseResult.postValue(it)
                                    updatesLoader.postValue(false)
                                }, {
                            updatesLoader.postValue(false)
                            updatesError.postValue(it.message)
                        }))
    }
}
