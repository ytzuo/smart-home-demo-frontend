package com.SmartHome.SmartHomeDemo.dds;

import android.content.Context;
import android.util.Log;

import com.zrdds.domain.DomainParticipant;
import com.zrdds.domain.DomainParticipantFactory;
import com.zrdds.domain.DomainParticipantFactoryQos;
import com.zrdds.domain.DomainParticipantQos;
import com.zrdds.infrastructure.Property_t;
import com.zrdds.infrastructure.StatusKind;

public class BaseDdsManager {
    private static final String TAG = "BaseDdsManager";
    protected DomainParticipant participant;
    protected static BaseDdsManager instance;
    protected Context context;

    protected static final int DOMAIN_ID = 0;

    protected BaseDdsManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized BaseDdsManager getInstance(Context context) {
        if (instance == null) {
            instance = new BaseDdsManager(context);
        }
        return instance;
    }

    public void initialize() {
        try {
            Log.i(TAG, "开始初始化ZRDDS基础组件...");

            System.setProperty("ZRDDS_HOME", "/sdcard/");

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

            // 创建域参与者
            participant = factory.create_participant(
                    DOMAIN_ID,
                    dpQos,
                    null,
                    StatusKind.STATUS_MASK_NONE
            );

            if (participant == null) {
                Log.e(TAG, "创建DomainParticipant失败");
                return;
            }
            Log.i(TAG, "✓ DomainParticipant创建成功，Domain ID: " + DOMAIN_ID);
            Log.i(TAG, "✓ ZRDDS基础组件初始化完成");

        } catch (Exception e) {
            Log.e(TAG, "ZRDDS基础组件初始化失败", e);
        }
    }

    public DomainParticipant getParticipant() {
        return participant;
    }

    public void cleanup() {
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
}
