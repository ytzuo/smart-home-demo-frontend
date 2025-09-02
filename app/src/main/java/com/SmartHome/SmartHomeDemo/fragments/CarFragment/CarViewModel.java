package com.SmartHome.SmartHomeDemo.fragments.CarFragment;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class CarViewModel extends AndroidViewModel {
    private MutableLiveData<CarItem> item;
    private CarItem carItem;

    public CarViewModel(Application application) {
        super(application);
        //测试数据
        item = new MutableLiveData<>();
        carItem = new CarItem("默认汽车名",
                "剩余油量：80%", "车辆位置：默认位置", true, false, false);
        item.setValue(carItem);
    }

    public LiveData<CarItem> getCarLiveData() {
        return item;
    }

    public void updateCarItem(CarItem newItem) {
        item.postValue(newItem);
    }
}
