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

import idl.SmartDemo03.HomeStatus;
import idl.SmartDemo03.HomeStatusDataReader;
import idl.SmartDemo03.HomeStatusSeq;
import idl.SmartDemo03.HomeStatusTypeSupport;


public class HomeStatusDdsManager {
    private static final String TAG = "HomeStatusDdsManager";

    private Topic topic;
    private Subscriber subscriber;
    private DataReader dataReader;

    private OnHomeStatusReceivedListener homeStatusListener;

    public interface OnHomeStatusReceivedListener {
        void onHomeStatusReceived(HomeStatus homeStatus);
    }

    private static final String TOPIC_NAME = "HomeStatus";

    public void initialize(BaseDdsManager baseManager) {
        try {
            Log.i(TAG, "开始初始化HomeStatus DDS组件...");

            ReturnCode_t result = HomeStatusTypeSupport.get_instance().register_type(
                    baseManager.getParticipant(),
                    null
            );

            if (result != ReturnCode_t.RETCODE_OK) {
                Log.e(TAG, "注册HomeStatus类型失败，错误码: " + result);
                return;
            }

            // 创建主题
            topic = baseManager.getParticipant().create_topic(
                    TOPIC_NAME,
                    HomeStatusTypeSupport.get_instance().get_type_name(),
                    DomainParticipant.TOPIC_QOS_DEFAULT,
                    null,
                    StatusKind.STATUS_MASK_NONE
            );

            if (topic == null) {
                Log.e(TAG, "创建HomeStatus主题失败");
                return;
            }

            Log.i(TAG, "✓ HomeStatus主题创建成功: " + TOPIC_NAME);

            createSubscriber(baseManager);

            Log.i(TAG, "✓ HomeStatus DDS组件初始化完成");
        } catch (Exception e) {
            Log.e(TAG, "HomeStatus DDS组件初始化失败", e);
        }
    }

    private void createSubscriber(BaseDdsManager baseManager) {
        try{
            // 创建订阅者
            subscriber = baseManager.getParticipant().create_subscriber(
                    DomainParticipant.SUBSCRIBER_QOS_DEFAULT,
                    null,
                    StatusKind.STATUS_MASK_NONE
            );

            if (subscriber == null) {
                Log.e(TAG, "创建HomeStatus Subscriber失败");
                return;
            }

            // 创建数据读取器，带监听器
            DataReaderListener readerListener = new DataReaderListener() {
                @Override
                public void on_data_available(DataReader reader) {
                    Log.i(TAG, "📨 收到新的HomeStatus数据！");
                    readHomeStatusData(reader);
                }

                @Override
                public void on_data_arrived(DataReader reader, Object obj, SampleInfo sampleInfo) {
                    Log.i(TAG, "📨 收到新的HomeStatus数据！");
                    readHomeStatusData(reader);
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
                Log.e(TAG, "创建HomeStatus DataReader失败");
                return;
            }

            Log.i(TAG, "✓ HomeStatus Subscriber和DataReader创建成功");
        } catch(Exception e) {
            Log.e(TAG, "创建HomeStatus Subscriber失败", e);
        }

    }
    private void readHomeStatusData(DataReader reader) {
        try {
            HomeStatusSeq homeStatuses = new HomeStatusSeq();
            SampleInfoSeq sampleInfos = new SampleInfoSeq();

            HomeStatusDataReader homeStatusDataReader = (HomeStatusDataReader) reader;

            ReturnCode_t result = homeStatusDataReader.take(homeStatuses, sampleInfos, 10,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

            if (result == ReturnCode_t.RETCODE_OK) {
                for (int i = 0; i < sampleInfos.length(); i++) {
                    if (sampleInfos.get_at(i).valid_data) {
                        HomeStatus receivedHomeStatus = homeStatuses.get_at(i);

                        // 通知监听器
                        if (homeStatusListener != null) {
                            final HomeStatus finalHomeStatus = new HomeStatus(receivedHomeStatus); // 复制数据
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    homeStatusListener.onHomeStatusReceived(finalHomeStatus);
                                }
                            });
                        }
                    }
                }

                // 返还数据
                homeStatusDataReader.return_loan(homeStatuses, sampleInfos);
            }

        } catch (Exception e) {
            Log.e(TAG, "读取HomeStatus数据时发生异常", e);
        }
    }

    public void setOnHomeStatusReceivedListener(OnHomeStatusReceivedListener listener) {
        this.homeStatusListener = listener;
    }
}
