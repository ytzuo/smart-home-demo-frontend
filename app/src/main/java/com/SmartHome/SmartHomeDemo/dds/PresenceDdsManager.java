package com.SmartHome.SmartHomeDemo.dds;

import android.util.Log;

import com.zrdds.domain.DomainParticipant;
import com.zrdds.infrastructure.InstanceHandle_t;
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
import com.zrdds.publication.DataWriter;
import com.zrdds.publication.Publisher;
import com.zrdds.subscription.DataReader;
import com.zrdds.subscription.DataReaderListener;
import com.zrdds.subscription.Subscriber;
import com.zrdds.topic.Topic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import idl.SmartDemo03.Presence;
import idl.SmartDemo03.PresenceDataReader;
import idl.SmartDemo03.PresenceDataWriter;
import idl.SmartDemo03.PresenceSeq;
import idl.SmartDemo03.PresenceTypeSupport;

public class PresenceDdsManager {
    private static final String TAG = "PresenceDdsManager";

    private Topic topic;
    private Publisher publisher;
    private Subscriber subscriber;
    private DataWriter dataWriter;
    private DataReader dataReader;

    private OnPresenceReceivedListener presenceListener;

    public interface OnPresenceReceivedListener {
        void onPresenceReceived(Presence presence);
    }

    private static final String TOPIC_NAME = "Presence";

    public void initialize(BaseDdsManager baseManager) {
        try {
            Log.i(TAG, "开始初始化Presence DDS组件...");

            // 注册Presence类型
            ReturnCode_t result = PresenceTypeSupport.get_instance().register_type(
                    baseManager.getParticipant(),
                    null
            );

            if (result != ReturnCode_t.RETCODE_OK) {
                Log.e(TAG, "注册Presence类型失败，错误码: " + result);
                return;
            }

            // 创建主题
            topic = baseManager.getParticipant().create_topic(
                    TOPIC_NAME,
                    PresenceTypeSupport.get_instance().get_type_name(),
                    DomainParticipant.TOPIC_QOS_DEFAULT,
                    null,
                    StatusKind.STATUS_MASK_NONE
            );

            if (topic == null) {
                Log.e(TAG, "创建Presence主题失败");
                return;
            }

            Log.i(TAG, "✓ Presence主题创建成功: " + TOPIC_NAME);

            createPublisher(baseManager);
            createSubscriber(baseManager);

            Log.i(TAG, "✓ Presence DDS组件初始化完成");
        } catch (Exception e) {
            Log.e(TAG, "Presence DDS组件初始化失败", e);
        }
    }

    private void createPublisher(BaseDdsManager baseManager) {
        try {
            // 创建发布者
            publisher = baseManager.getParticipant().create_publisher(
                    DomainParticipant.PUBLISHER_QOS_DEFAULT,
                    null,
                    StatusKind.STATUS_MASK_NONE
            );

            if (publisher == null) {
                Log.e(TAG, "创建Presence Publisher失败");
                return;
            }

            // 创建数据写入器
            dataWriter = publisher.create_datawriter(
                    topic,
                    Publisher.DATAWRITER_QOS_DEFAULT,
                    null,
                    StatusKind.STATUS_MASK_NONE
            );

            if (dataWriter == null) {
                Log.e(TAG, "创建Presence DataWriter失败");
                return;
            }

            Log.i(TAG, "✓ Presence Publisher和DataWriter创建成功");
        } catch (Exception e) {
            Log.e(TAG, "创建Presence Publisher失败", e);
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
                Log.e(TAG, "创建Presence Subscriber失败");
                return;
            }

            // 创建数据读取器，带监听器
            DataReaderListener readerListener = new DataReaderListener() {
                @Override
                public void on_data_available(DataReader reader) {
                    Log.i(TAG, "📨 收到新的Presence数据！");
                    readPresenceData(reader);
                }

                @Override
                public void on_data_arrived(DataReader reader, Object obj, SampleInfo sampleInfo) {
                    Log.i(TAG, "📨 收到新的Presence数据！");
                    readPresenceData(reader);
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
                Log.e(TAG, "创建Presence DataReader失败");
                return;
            }

            Log.i(TAG, "✓ Presence Subscriber和DataReader创建成功");

        } catch (Exception e) {
            Log.e(TAG, "创建Presence Subscriber失败", e);
        }
    }

    private void readPresenceData(DataReader reader) {
        try {
            PresenceSeq presences = new PresenceSeq();
            SampleInfoSeq sampleInfos = new SampleInfoSeq();

            PresenceDataReader presenceDataReader = (PresenceDataReader) reader;

            ReturnCode_t result = presenceDataReader.take(presences, sampleInfos, 10,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

            if (result == ReturnCode_t.RETCODE_OK) {
                for (int i = 0; i < sampleInfos.length(); i++) {
                    if (sampleInfos.get_at(i).valid_data) {
                        Presence receivedPresence = presences.get_at(i);

                        // 通知监听器
                        if (presenceListener != null) {
                            final Presence finalPresence = new Presence(receivedPresence); // 复制数据
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    presenceListener.onPresenceReceived(finalPresence);
                                }
                            });
                        }
                    }
                }

                // 返还数据
                presenceDataReader.return_loan(presences, sampleInfos);
            }

        } catch (Exception e) {
            Log.e(TAG, "读取Presence数据时发生异常", e);
        }
    }

    /**
     * 发布Presence消息
     * @param inRange 设备是否在范围内
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     */
    public void publishPresence(boolean inRange, String deviceId, String deviceType) {
        try {
            if (dataWriter == null) {
                Log.e(TAG, "Presence DataWriter未初始化");
                return;
            }

            // 创建Presence对象
            Presence presence   = new Presence();
            presence.inRange    = inRange;
            presence.deviceId   = deviceId;
            presence.deviceType = deviceType;
            presence.timeStamp  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            // 转换为具体的PresenceDataWriter
            PresenceDataWriter presenceDataWriter = (PresenceDataWriter) dataWriter;

            // 发送数据
            ReturnCode_t result = presenceDataWriter.write(presence, InstanceHandle_t.HANDLE_NIL_NATIVE);

            if (result == ReturnCode_t.RETCODE_OK) {
                Log.i(TAG, "Presence消息发送成功: " + deviceId + ", inRange: " + inRange);
            } else {
                Log.e(TAG, "Presence消息发送失败，错误码: " + result);
            }
        } catch (Exception e) {
            Log.e(TAG, "发送Presence消息时发生异常", e);
        }
    }

    /**
     * 当设备加入网络时调用
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     */
    public void onDeviceJoined(String deviceId, String deviceType) {
        publishPresence(true, deviceId, deviceType);
    }

    /**
     * 当设备离开网络时调用
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     */
    public void onDeviceLeft(String deviceId, String deviceType) {
        publishPresence(false, deviceId, deviceType);
    }

    public void setOnPresenceReceivedListener(OnPresenceReceivedListener listener) {
        this.presenceListener = listener;
    }
}
