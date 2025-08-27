package com.SmartHome.SmartHomeDemo;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zrdds.domain.DomainParticipant;
import com.zrdds.domain.DomainParticipantFactory;
import com.zrdds.domain.DomainParticipantFactoryQos;
import com.zrdds.domain.DomainParticipantQos;
import com.zrdds.topic.Topic;
import com.zrdds.publication.Publisher;
import com.zrdds.publication.DataWriter;
import com.zrdds.subscription.Subscriber;
import com.zrdds.subscription.DataReader;
import com.zrdds.subscription.DataReaderListener;
import com.zrdds.infrastructure.SampleInfo;
import com.zrdds.infrastructure.StatusKind;
import com.zrdds.infrastructure.*;

//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//    }
//}

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        System.loadLibrary("ZRDDS_JAVA");
        System.setProperty("ZRDDS_HOME", "/sdcard/");

        initializeZRDDS();
    }

    private EditText msgEdit = null;
    private EditText msgDisplay = null;
    public void onSendClicked(View v) {
        if (msgEdit == null) {
            msgEdit = findViewById(R.id.msgEdit);
        }
        if (msgDisplay == null) {
            msgDisplay = findViewById(R.id.msgDisplay);
        }
        if (msgEdit.getText().length() == 0) {
            return;
        }
        sendZRDDSData(msgEdit.getText().toString());
        //displayMsg(msgEdit.getText().toString());
        msgEdit.getText().clear();
    }
    private void displayMsg(String msg) {
        msgDisplay.getText().append("\n");
        msgDisplay.getText().append(msg);
    }

    /**
     * 初始化ZRDDS组件
     */
    private void initializeZRDDS() {
        try {

            Log.i(TAG, "开始初始化ZRDDS...");
            DomainParticipantFactoryQos dpfQos = new DomainParticipantFactoryQos();
            dpfQos.dds_log.file_mask = 0;
            dpfQos.dds_log.console_mask = 0xffff;
            Property_t property = new Property_t();
            property.name = "sysctl.global.licence";
            property.value = "data:UserName: \nAuth Date: 2020/09/15:19:17:26\nExpire Date: 2025/10/21:19:17:26\nMACS:\nunlimited\nHDS:\nunlimited\nSignature:\nb4b93ac94879a73959465ad0692722934efb100a1069d1e91d4fc14596483cf651496531f7376f389b2a6cea9dc4b276f8cdd3ce171f2c333a5f6061e0033a94889282b1d142ca3709b69e6e88cd24252818bd543c1f66a1ae905bdb8b854e03055a1535fa262570fbefcdb7c05b63f872809cd57f82dfcc72cc495eee824ff0\nLastVerifyDate:2024/11/21:11:01:5022325d7c7825924f6f8c0ab42a65414c";
            dpfQos.property.value.ensure_length(0, 1);
            dpfQos.property.value.append(property);

            DomainParticipantFactory factory = DomainParticipantFactory.get_instance_w_qos(dpfQos);
            if (factory == null) {
                Log.e(TAG, "无法获取DomainParticipantFactory实例");
                return;
            }
            Log.i(TAG, "✓ DomainParticipantFactory创建成功");

            DomainParticipantQos dpQos = new DomainParticipantQos();
            factory.get_default_participant_qos(dpQos);
            dpQos.discovery_config.participant_liveliness_lease_duration.sec = 10;
            dpQos.discovery_config.participant_liveliness_assert_period.sec = 1;
            // 2. 创建域参与者
            participant = factory.create_participant(
                    DOMAIN_ID,
                    dpQos,
                    null, // listener
                    StatusKind.STATUS_MASK_NONE
            );

            if (participant == null) {
                Log.e(TAG, "创建DomainParticipant失败");
                return;
            }
            Log.i(TAG, "✓ DomainParticipant创建成功，Domain ID: " + DOMAIN_ID);

            // 3. 创建内置Bytes主题
            createBytesTopic();

            // 4. 创建发布者和订阅者
            createPublisher();
            createSubscriber();

            Log.i(TAG, "✓ ZRDDS初始化完成");

        } catch (Exception e) {
            Log.e(TAG, "ZRDDS初始化失败", e);
        }
    }

    /**
     * 创建内置Bytes主题
     */
    private void createBytesTopic() {
        try {
            // 注册内置Bytes类型
            ReturnCode_t result = BytesTypeSupport.get_instance().register_type(
                    participant,
                    null
            );

            if (result != ReturnCode_t.RETCODE_OK) {
                Log.e(TAG, "注册Bytes类型失败，错误码: " + result);
                return;
            }

            // 创建主题
            topic = participant.create_topic(
                    TOPIC_NAME,
                    BytesTypeSupport.get_instance().get_type_name(),
                    DomainParticipant.TOPIC_QOS_DEFAULT,
                    null, // listener
                    StatusKind.STATUS_MASK_NONE
            );

            if (topic == null) {
                Log.e(TAG, "创建Topic失败");
                return;
            }

            Log.i(TAG, "✓ Bytes主题创建成功: " + TOPIC_NAME);

        } catch (Exception e) {
            Log.e(TAG, "创建Bytes主题失败", e);
        }
    }

    /**
     * 创建发布者和数据写入器
     */
    private void createPublisher() {
        try {
            // 创建发布者
            publisher = participant.create_publisher(
                    DomainParticipant.PUBLISHER_QOS_DEFAULT,
                    null, // listener
                    StatusKind.STATUS_MASK_NONE
            );

            if (publisher == null) {
                Log.e(TAG, "创建Publisher失败");
                return;
            }

            // 创建数据写入器
            dataWriter = publisher.create_datawriter(
                    topic,
                    Publisher.DATAWRITER_QOS_DEFAULT,
                    null, // listener
                    StatusKind.STATUS_MASK_NONE
            );

            if (dataWriter == null) {
                Log.e(TAG, "创建DataWriter失败");
                return;
            }

            Log.i(TAG, "✓ Publisher和DataWriter创建成功");

        } catch (Exception e) {
            Log.e(TAG, "创建Publisher失败", e);
        }
    }

    /**
     * 创建订阅者和数据读取器
     */
    private void createSubscriber() {
        try {
            // 创建订阅者
            subscriber = participant.create_subscriber(
                    DomainParticipant.SUBSCRIBER_QOS_DEFAULT,
                    null, // listener
                    StatusKind.STATUS_MASK_NONE
            );

            if (subscriber == null) {
                Log.e(TAG, "创建Subscriber失败");
                return;
            }

            // 创建数据读取器，带监听器
            DataReaderListener readerListener = new DataReaderListener() {
                @Override
                public void on_data_available(DataReader reader) {
                    Log.i(TAG, "📨 收到新数据！");
                    readZRDDSData(reader);
                }

                @Override
                public void on_data_arrived(DataReader reader, Object obj, SampleInfo sampleInfo) {
                    Log.i(TAG, "📨 收到新数据！");
                    readZRDDSData(reader);
                }

                @Override
                public void on_sample_lost(DataReader reader, SampleLostStatus status) {
                    Log.w(TAG, "数据丢失: " + status.total_count);
                }

                @Override
                public void on_sample_rejected(DataReader reader, SampleRejectedStatus status) {
                    Log.w(TAG, "数据被拒绝: " + status.total_count);
                }

                @Override
                public void on_requested_deadline_missed(DataReader reader, RequestedDeadlineMissedStatus status) {
                    Log.w(TAG, "请求截止时间错过: " + status.total_count);
                }

                @Override
                public void on_requested_incompatible_qos(DataReader reader, RequestedIncompatibleQosStatus status) {
                    Log.w(TAG, "请求的QoS不兼容: " + status.total_count);
                }

                @Override
                public void on_liveliness_changed(DataReader reader, LivelinessChangedStatus status) {
                    Log.i(TAG, "存活状态改变: alive=" + status.alive_count + ", not_alive=" + status.not_alive_count);
                }

                @Override
                public void on_subscription_matched(DataReader reader, SubscriptionMatchedStatus status) {
                    Log.i(TAG, "订阅匹配: current=" + status.current_count + ", total=" + status.total_count);
                }
            };

            dataReader = subscriber.create_datareader(
                    topic,
                    Subscriber.DATAREADER_QOS_DEFAULT,
                    readerListener,
                    StatusKind.STATUS_MASK_ALL
            );

            if (dataReader == null) {
                Log.e(TAG, "创建DataReader失败");
                return;
            }
//            WaitSet ws = new WaitSet();
//            ws.attach_condition(dataReader.create_readcondition(
//                    SampleStateKind.NOT_READ_SAMPLE_STATE,
//                    ViewStateKind.ANY_VIEW_STATE,
//                    InstanceStateKind.ANY_INSTANCE_STATE));
//            Thread recvThread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    while (true) {
//                        ConditionSeq activeConditionSeq = new ConditionSeq();
//                        if (ws.wait(activeConditionSeq, Duration_t.DURATION_INFINITE) == ReturnCode_t.RETCODE_OK) {
//                            // 读取并显示数据
//                            readZRDDSData(dataReader);
//                        }
//                    }
//                }
//            });
//            recvThread.start();
            Log.i(TAG, "✓ Subscriber和DataReader创建成功");

        } catch (Exception e) {
            Log.e(TAG, "创建Subscriber失败", e);
        }
    }

    /**
     * 发送ZRDDS数据（按钮点击事件）
     */
    private void sendZRDDSData(String msg) {
        try {
            if (dataWriter == null) {
                displayMsg("ZRDDS未初始化完成");
                return;
            }

            // 准备要发送的数据
            messageCounter++;
            byte[] data = msg.getBytes("UTF-8");

            // 创建Bytes数据对象
            Bytes sample = new Bytes();
            sample.value.from_array(data, data.length);
            BytesDataWriter bytesDataWriter = (BytesDataWriter) dataWriter;
            // 发送数据
            ReturnCode_t result = bytesDataWriter.write(sample, InstanceHandle_t.HANDLE_NIL_NATIVE);

            if (result == ReturnCode_t.RETCODE_OK) {
                String successMsg = "✓ 数据发送成功 #" + messageCounter + ": " + msg;
                Log.i(TAG, successMsg);
                displayMsg(successMsg);
            } else {
                String errorMsg = "❌ 数据发送失败，错误码: " + result;
                Log.e(TAG, errorMsg);
                displayMsg(errorMsg);
            }

        } catch (Exception e) {
            Log.e(TAG, "发送数据时发生异常", e);
            displayMsg("发送失败: " + e.getMessage());
        }
        //Publish.pub();
    }

    /**
     * 读取ZRDDS数据
     */
    private void readZRDDSData(DataReader reader) {
        try {
            // 读取数据
            SampleInfoSeq sampleInfos = new SampleInfoSeq();
            BytesSeq samples = new BytesSeq();

            BytesDataReader bytesDataReader = (BytesDataReader) reader;

            ReturnCode_t result = bytesDataReader.take(samples, sampleInfos, 10,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

            if (result == ReturnCode_t.RETCODE_OK) {
                for (int i = 0; i < sampleInfos.length(); i++) {
                    if (sampleInfos.get_at(i).valid_data) {
                        // 处理接收到的数据
                        Bytes receivedData = samples.get_at(i);
                        String message = new String(
                                receivedData.value.get_contiguous_buffer(), 0, receivedData.value.length(),
                                "UTF-8");
                        Log.i(TAG, "📨 接收到数据: " + message);
                        // 在UI线程显示消息
                        this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Log.i(TAG, "收到: " + message);
                                    displayMsg("收到: " + message);
                                }catch (Exception e) {
                                    Log.e("UIThread", "Exception", e);
                                }
                            }
                        });
                    }
                }

                // 返还数据
                bytesDataReader.return_loan(samples, sampleInfos);
            }

        } catch (Exception e) {
            Log.e(TAG, "读取数据时发生异常", e);
        }
    }

    /**
     * 清理ZRDDS资源
     */
    private void cleanupZRDDS() {
        try {
            Log.i(TAG, "开始清理ZRDDS资源...");

            if (participant != null) {
                // 删除所有实体
                participant.delete_contained_entities();

                // 删除域参与者
                DomainParticipantFactory factory = DomainParticipantFactory.get_instance();
                if (factory != null) {
                    factory.delete_participant(participant);
                }

                participant = null;
            }

            Log.i(TAG, "✓ ZRDDS资源清理完成");

        } catch (Exception e) {
            Log.e(TAG, "清理ZRDDS资源时发生异常", e);
        }
    }


    private static final String TAG = "ZRDDSDemo";
    private static final String TOPIC_NAME = "BytesTestTopic";
    private static final int DOMAIN_ID = 0;

    // ZRDDS 组件
    private DomainParticipant participant;
    private Publisher publisher;
    private Subscriber subscriber;
    private Topic topic;
    private DataWriter dataWriter;
    private DataReader dataReader;

    // 数据包计数器
    private int messageCounter = 0;
}