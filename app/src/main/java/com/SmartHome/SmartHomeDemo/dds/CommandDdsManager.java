package com.SmartHome.SmartHomeDemo.dds;

import android.util.Log;

import com.zrdds.domain.DomainParticipant;
import com.zrdds.infrastructure.InstanceHandle_t;
import com.zrdds.infrastructure.ReturnCode_t;
import com.zrdds.infrastructure.StatusKind;
import com.zrdds.publication.DataWriter;
import com.zrdds.publication.Publisher;
import com.zrdds.topic.Topic;

import idl.SmartDemo03.Command;
import idl.SmartDemo03.CommandDataWriter;
import idl.SmartDemo03.CommandTypeSupport;


public class CommandDdsManager {
    private static final String TAG = "CommandDdsManager";

    private Topic topic;
    private Publisher publisher;
    private DataWriter dataWriter;

    private static final String TOPIC_NAME = "CommandTopic";
    public void initialize(BaseDdsManager baseManager) {
        try{
            Log.i(TAG, "开始初始化Command DDS组件...");

            ReturnCode_t result = CommandTypeSupport.get_instance().register_type(
                    baseManager.getParticipant(),
                    null
            );

            if(result != ReturnCode_t.RETCODE_OK) {
                Log.e(TAG, "注册Command类型失败，错误码: " + result);
                return;
            }

            // 创建主题
            topic = baseManager.getParticipant().create_topic(
                    TOPIC_NAME,
                    CommandTypeSupport.get_instance().get_type_name(),
                    DomainParticipant.TOPIC_QOS_DEFAULT,
                    null,
                    StatusKind.STATUS_MASK_NONE
            );

            if (topic == null) {
                Log.e(TAG, "创建Command主题失败");
                return;
            }

            Log.i(TAG, "✓ Command主题创建成功: " + TOPIC_NAME);

            createPublisher(baseManager);

            Log.i(TAG, "✓ Command DDS组件初始化完成");
        } catch(Exception e) {
            Log.e(TAG, "Command DDS组件初始化失败", e);
        }
    }

    private void createPublisher(BaseDdsManager baseManager) {
        try{
            publisher = baseManager.getParticipant().create_publisher(
                    DomainParticipant.PUBLISHER_QOS_DEFAULT,
                    null,
                    StatusKind.STATUS_MASK_NONE
            );

            if (publisher == null) {
                Log.e(TAG, "创建Command Publisher失败");
                return;
            }

            dataWriter = publisher.create_datawriter(
                    topic,
                    Publisher.DATAWRITER_QOS_DEFAULT,
                    null,
                    StatusKind.STATUS_MASK_ALL
            );

            if (dataWriter == null) {
                Log.e(TAG, "创建Command DateWriter失败");
                return;
            }

            Log.i(TAG, "✓ Command Publisher和DataWriter创建成功");
        } catch(Exception e) {
            Log.e(TAG, "创建Command Publisher失败", e);
        }
    }

    /**
     * 发送Command消息
     * @param command 要发送的命令对象
     */
    public void sendCommand(Command command) {
        try {
            if (dataWriter == null) {
                Log.e(TAG, "Command DataWriter未初始化");
                return;
            }

            // 转换为具体的CommandDataWriter
            CommandDataWriter commandDataWriter = (CommandDataWriter) dataWriter;

            // 发送数据
            ReturnCode_t result = commandDataWriter.write(command, InstanceHandle_t.HANDLE_NIL_NATIVE);

            if (result == ReturnCode_t.RETCODE_OK) {
                Log.i(TAG, "✓ Command数据发送成功: deviceId=" + command.deviceId +
                        ", action=" + command.action + ", value=" + command.value);
            } else {
                Log.e(TAG, "❌ Command数据发送失败，错误码: " + result);
            }

        } catch (Exception e) {
            Log.e(TAG, "发送Command数据时发生异常", e);
        }
    }
    /**
     * 创建并发送Command消息
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param action 执行动作
     * @param value 值
     * @param timeStamp 时间戳
     */
    public void sendCommand(String deviceId, String deviceType, String action, float value, String timeStamp) {
        Command command = new Command();
        command.deviceId = deviceId;
        command.deviceType = deviceType;
        command.action = action;
        command.value = value;
        command.timeStamp = timeStamp;

        sendCommand(command);
    }
}
