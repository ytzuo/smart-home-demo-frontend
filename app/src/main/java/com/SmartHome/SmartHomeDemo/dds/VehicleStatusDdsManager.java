package com.SmartHome.SmartHomeDemo.dds;

import android.util.Log;

import com.zrdds.domain.DomainParticipant;
import com.zrdds.infrastructure.InstanceStateKind;
import com.zrdds.infrastructure.LivelinessChangedStatus;
import com.zrdds.infrastructure.RequestedDeadlineMissedStatus;
import com.zrdds.infrastructure.RequestedIncompatibleQosStatus;
import com.zrdds.infrastructure.ReturnCode_t;
import com.zrdds.infrastructure.SampleInfo;
import com.zrdds.infrastructure.SampleInfoSeq;
import com.zrdds.infrastructure.SampleLostStatus;
import com.zrdds.infrastructure.SampleRejectedStatus;
import com.zrdds.infrastructure.SampleStateKind;
import com.zrdds.infrastructure.StatusKind;
import com.zrdds.infrastructure.SubscriptionMatchedStatus;
import com.zrdds.infrastructure.ViewStateKind;
import com.zrdds.subscription.DataReader;
import com.zrdds.subscription.DataReaderListener;
import com.zrdds.subscription.Subscriber;
import com.zrdds.topic.Topic;

import idl.SmartDemo03.Alert;
import idl.SmartDemo03.AlertDataReader;
import idl.SmartDemo03.AlertSeq;
import idl.SmartDemo03.AlertTypeSupport;
import idl.SmartDemo03.VehicleStatus;
import idl.SmartDemo03.VehicleStatusDataReader;
import idl.SmartDemo03.VehicleStatusSeq;
import idl.SmartDemo03.VehicleStatusTypeSupport;

public class VehicleStatusDdsManager {
    private static final String TAG = "VehicleStatusDdsManager";

    private Topic topic;
    private Subscriber subscriber;
    private DataReader dataReader;

    private OnVehicleStatusReceivedListener vehicleStatusListener;

    public interface OnVehicleStatusReceivedListener {
        void onVehicleStatusReceived(VehicleStatus status);
    }

    private static final String TOPIC_NAME = "VehicleStatus";

    public void initialize(BaseDdsManager baseManager) {
        try {
            Log.i(TAG, "开始初始化VehicleStatus DDS组件...");

            // 注册VehicleStatus类型
            ReturnCode_t result = VehicleStatusTypeSupport.get_instance().register_type(
                    baseManager.getParticipant(),
                    null
            );

            if (result != ReturnCode_t.RETCODE_OK) {
                Log.e(TAG, "注册VehicleStatus类型失败，错误码: " + result);
                return;
            }

            // 创建主题
            topic = baseManager.getParticipant().create_topic(
                    TOPIC_NAME,
                    VehicleStatusTypeSupport.get_instance().get_type_name(),
                    DomainParticipant.TOPIC_QOS_DEFAULT,
                    null,
                    StatusKind.STATUS_MASK_NONE
            );

            if (topic == null) {
                Log.e(TAG, "创建VehicleStatus主题失败");
                return;
            }

            Log.i(TAG, "✓ VehicleStatus主题创建成功: " + TOPIC_NAME);

            createSubscriber(baseManager);

            Log.i(TAG, "✓ VehicleStatus DDS组件初始化完成");

        } catch (Exception e) {
            Log.e(TAG, "VehicleStatus DDS组件初始化失败", e);
        }
    }

    private void createSubscriber(BaseDdsManager baseManager) {
        try {
            // 创建订阅者
            subscriber = baseManager.getParticipant().create_subscriber(
                    DomainParticipant.SUBSCRIBER_QOS_DEFAULT,
                    null,
                    StatusKind.STATUS_MASK_NONE
            );

            if (subscriber == null) {
                Log.e(TAG, "创建VehicleStatus Subscriber失败");
                return;
            }

            // 创建数据读取器，带监听器
            DataReaderListener readerListener = new DataReaderListener() {
                @Override
                public void on_data_available(DataReader reader) {
                    Log.i(TAG, "📨 收到新的VehicleStatus数据！");
                    readVehicleStatusData(reader);
                }

                @Override
                public void on_data_arrived(DataReader reader, Object obj, SampleInfo sampleInfo) {
                    Log.i(TAG, "📨 收到新的VehicleStatus数据！");
                    readVehicleStatusData(reader);
                }

                // 实现其他必要的回调方法
                @Override
                public void on_sample_lost(DataReader reader, SampleLostStatus status) {}

                @Override
                public void on_sample_rejected(DataReader reader, SampleRejectedStatus status) {}

                @Override
                public void on_requested_deadline_missed(DataReader reader, RequestedDeadlineMissedStatus status) {}

                @Override
                public void on_requested_incompatible_qos(DataReader reader, RequestedIncompatibleQosStatus status) {}

                @Override
                public void on_liveliness_changed(DataReader reader, LivelinessChangedStatus status) {}

                @Override
                public void on_subscription_matched(DataReader reader, SubscriptionMatchedStatus status) {}
            };

            dataReader = subscriber.create_datareader(
                    topic,
                    Subscriber.DATAREADER_QOS_DEFAULT,
                    readerListener,
                    StatusKind.STATUS_MASK_ALL
            );

            if (dataReader == null) {
                Log.e(TAG, "创建VehicleStatus DataReader失败");
                return;
            }

            Log.i(TAG, "✓ VehicleStatus Subscriber和DataReader创建成功");

        } catch (Exception e) {
            Log.e(TAG, "创建VehicleStatus Subscriber失败", e);
        }
    }

    private void readVehicleStatusData(DataReader reader) {
        try {
            VehicleStatusSeq vehicleStatuses = new VehicleStatusSeq();
            SampleInfoSeq sampleInfos = new SampleInfoSeq();

            VehicleStatusDataReader vehicleStatusDataReader = (VehicleStatusDataReader) reader;

            ReturnCode_t result = vehicleStatusDataReader.take(vehicleStatuses, sampleInfos, 10,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

            if (result == ReturnCode_t.RETCODE_OK) {
                for (int i = 0; i < sampleInfos.length(); i++) {
                    if (sampleInfos.get_at(i).valid_data) {
                        VehicleStatus receivedVehicleStatus = vehicleStatuses.get_at(i);

                        // 通知监听器
                        if (vehicleStatusListener != null) {
                            final VehicleStatus finalVehicleStatus = new VehicleStatus(receivedVehicleStatus); // 复制数据
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    vehicleStatusListener.onVehicleStatusReceived(finalVehicleStatus);
                                }
                            });
                        }
                    }
                }

                // 返还数据
                vehicleStatusDataReader.return_loan(vehicleStatuses, sampleInfos);
            }

        } catch (Exception e) {
            Log.e(TAG, "读取VehicleStatus数据时发生异常", e);
        }
    }

    public void setOnVehicleStatusReceivedListener(OnVehicleStatusReceivedListener listener) {
        this.vehicleStatusListener = listener;
    }
}
