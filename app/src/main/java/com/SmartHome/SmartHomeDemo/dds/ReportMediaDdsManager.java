//package com.SmartHome.SmartHomeDemo.dds;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.util.Log;
//
//import com.zrdds.domain.DomainParticipant;
//import com.zrdds.infrastructure.InstanceStateKind;
//import com.zrdds.infrastructure.LivelinessChangedStatus;
//import com.zrdds.infrastructure.RequestedDeadlineMissedStatus;
//import com.zrdds.infrastructure.RequestedIncompatibleQosStatus;
//import com.zrdds.infrastructure.ReturnCode_t;
//import com.zrdds.infrastructure.SampleInfo;
//import com.zrdds.infrastructure.SampleInfoSeq;
//import com.zrdds.infrastructure.SampleLostStatus;
//import com.zrdds.infrastructure.SampleRejectedStatus;
//import com.zrdds.infrastructure.SampleStateKind;
//import com.zrdds.infrastructure.StatusKind;
//import com.zrdds.infrastructure.SubscriptionMatchedStatus;
//import com.zrdds.infrastructure.ViewStateKind;
//import com.zrdds.subscription.DataReader;
//import com.zrdds.subscription.DataReaderListener;
//import com.zrdds.subscription.Subscriber;
//import com.zrdds.topic.Topic;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import idl.SmartDemo03.AlertMedia;
//import idl.SmartDemo03.AlertMediaDataReader;
//import idl.SmartDemo03.AlertMediaSeq;
//
//
//public class ReportMediaDdsManager {
//    private static final String TAG = "ReportMediaDdsManager";
//
//    private Topic topic;
//    private Subscriber subscriber;
//    private DataReader dataReader;
//
//    private OnMediaReceivedListener mediaListener;
//
//    // 用于存储接收到的图片数据
//    private Map<Integer, MediaReceiver> mediaReceivers = new HashMap<>();
//
//    // 应用内部存储路径
//    //private String savePath;
//
//    public interface OnMediaReceivedListener {
//        void onMediaReceived(int alertId, Bitmap bitmap, String deviceId, String deviceType);
//    }
//
//    private static final String TOPIC_NAME = "ReportMedia";
//
//    public void initialize(BaseDdsManager baseManager, String savePath) {
//        //this.savePath = savePath;
//
//        try {
//            Log.i(TAG, "开始初始化ReportMedia DDS组件...");
//
//            // 注册ReportMedia类型
//            ReturnCode_t result = ReportMediaTypeSupport.get_instance().register_type(
//                    baseManager.getParticipant(),
//                    null
//            );
//
//            if (result != ReturnCode_t.RETCODE_OK) {
//                Log.e(TAG, "注册ReportMedia类型失败，错误码: " + result);
//                return;
//            }
//
//            // 创建主题
//            topic = baseManager.getParticipant().create_topic(
//                    TOPIC_NAME,
//                    ReportMediaTypeSupport.get_instance().get_type_name(),
//                    DomainParticipant.TOPIC_QOS_DEFAULT,
//                    null,
//                    StatusKind.STATUS_MASK_NONE
//            );
//
//            if (topic == null) {
//                Log.e(TAG, "创建ReportMedia主题失败");
//                return;
//            }
//
//            Log.i(TAG, "✓ ReportMedia主题创建成功: " + TOPIC_NAME);
//
//            createSubscriber(baseManager);
//
//            Log.i(TAG, "✓ ReportMedia DDS组件初始化完成");
//        } catch (Exception e) {
//            Log.e(TAG, "ReportMedia DDS组件初始化失败", e);
//        }
//    }
//
//
//        // 内部类：管理单个媒体流的数据接收（与Java端保持一致）
//    private class MediaReceiver {
//        private byte[] data;
//        private boolean[] receivedChunks;
//        private int receivedBytes = 0;
//        private static final int CHUNK_SIZE = 4096;
//
//        public MediaReceiver(int totalSize) {
//            data = new byte[totalSize];
//            receivedChunks = new boolean[(int) Math.ceil((double) totalSize / CHUNK_SIZE)];
//            Log.i(TAG, "创建MediaReceiver: 总大小=" + totalSize + ", 块数=" + receivedChunks.length);
//        }
//
//        public boolean addChunk(int chunkSeq, byte[] chunkData) {
//            Log.d(TAG, "添加数据块: 序号=" + chunkSeq + ", 大小=" + chunkData.length);
//
//            if (chunkSeq >= receivedChunks.length) {
//                Log.e(TAG, "块序号超出范围: " + chunkSeq + " >= " + receivedChunks.length);
//                return false;
//            }
//
//            if (!receivedChunks[chunkSeq]) {
//                int offset = chunkSeq * CHUNK_SIZE;
//                int copyLength = Math.min(chunkData.length, data.length - offset);
//                Log.d(TAG, "复制数据: offset=" + offset + ", length=" + copyLength);
//                System.arraycopy(chunkData, 0, data, offset, copyLength);
//                receivedChunks[chunkSeq] = true;
//                receivedBytes += copyLength;
//            }
//
//            // 检查是否所有块都已接收
//            for (boolean received : receivedChunks) {
//                if (!received) {
//                    Log.d(TAG, "仍有未接收的块");
//                    return false;
//                }
//            }
//            Log.i(TAG, "所有数据块接收完成");
//            return true;
//        }
//
//        public byte[] getData() {
//            return data;
//        }
//
//        public double getProgress() {
//            return (double) receivedBytes / data.length;
//        }
//
//        public int getTotalChunks() {
//            return receivedChunks.length;
//        }
//
//        public int getReceivedBytes() {
//            return receivedBytes;
//        }
//
//    }
//
//    private void createSubscriber(BaseDdsManager baseManager) {
//        try {
//            // 创建订阅者
//            subscriber = baseManager.getParticipant().create_subscriber(
//                    DomainParticipant.SUBSCRIBER_QOS_DEFAULT,
//                    null,
//                    StatusKind.STATUS_MASK_NONE
//            );
//
//            if (subscriber == null) {
//                Log.e(TAG, "创建ReportMedia Subscriber失败");
//                return;
//            }
//
//            // 创建数据读取器，带监听器
//            DataReaderListener readerListener = new DataReaderListener() {
//                @Override
//                public void on_data_available(DataReader reader) {
//                    Log.i(TAG, "📨 收到新的Media数据！");
//                    readMediaData(reader);
//                }
//
//                @Override
//                public void on_data_arrived(DataReader reader, Object obj, SampleInfo sampleInfo) {
//                    Log.i(TAG, "📨 收到新的Media数据！");
//                    readMediaData(reader);
//                }
//
//                // 实现其他必要的回调方法
//                @Override
//                public void on_sample_lost(DataReader reader, SampleLostStatus status) {}
//
//                @Override
//                public void on_sample_rejected(DataReader reader, SampleRejectedStatus status) {}
//
//                @Override
//                public void on_requested_deadline_missed(DataReader reader, RequestedDeadlineMissedStatus status) {}
//
//                @Override
//                public void on_requested_incompatible_qos(DataReader reader, RequestedIncompatibleQosStatus status) {}
//
//                @Override
//                public void on_liveliness_changed(DataReader reader, LivelinessChangedStatus status) {}
//
//                @Override
//                public void on_subscription_matched(DataReader reader, SubscriptionMatchedStatus status) {}
//            };
//
//            dataReader = subscriber.create_datareader(
//                    topic,
//                    Subscriber.DATAREADER_QOS_DEFAULT,
//                    readerListener,
//                    StatusKind.STATUS_MASK_ALL
//            );
//
//            if (dataReader == null) {
//                Log.e(TAG, "创建ReportMedia DataReader失败");
//                return;
//            }
//
//            Log.i(TAG, "✓ ReportMedia Subscriber和DataReader创建成功");
//
//        } catch (Exception e) {
//            Log.e(TAG, "创建ReportMedia Subscriber失败", e);
//        }
//    }
//
//    private void readMediaData(DataReader reader) {
//        try {
//            ReportMediaSeq mediaSeq = new ReportMediaSeq();
//            SampleInfoSeq sampleInfos = new SampleInfoSeq();
//
//            ReportMediaDataReader mediaDataReader = (ReportMediaDataReader) reader;
//
//            ReturnCode_t result = mediaDataReader.take(mediaSeq, sampleInfos, 10,
//                    SampleStateKind.ANY_SAMPLE_STATE,
//                    ViewStateKind.ANY_VIEW_STATE,
//                    InstanceStateKind.ANY_INSTANCE_STATE);
//
//            if (result == ReturnCode_t.RETCODE_OK) {
//                for (int i = 0; i < sampleInfos.length(); i++) {
//                    if (sampleInfos.get_at(i).valid_data) {
//                        ReportMedia receivedMedia = mediaSeq.get_at(i);
//                        processMediaChunk(receivedMedia);
//                    }
//                }
//
//                // 返还数据
//                mediaDataReader.return_loan(mediaSeq, sampleInfos);
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, "读取ReportMedia数据时发生异常", e);
//        }
//    }
//
//    private void processMediaChunk(ReportMedia media) {
//        int reportId = media.reportId;
//
//        // 如果是新的媒体流，创建新的接收器
//        if (!mediaReceivers.containsKey(reportId)) {
//            mediaReceivers.put(reportId, new MediaReceiver(media.total_size));
//            Log.i(TAG, String.format("开始接收媒体流: alertId=%d, deviceId=%s, type=%d, size=%d bytes",
//                    reportId, media.deviceId, media.media_type, media.total_size));
//        }
//
//        // 添加数据块
//        MediaReceiver receiver = mediaReceivers.get(reportId);
//
//        // 处理Blob对象的方式 - 先获取byte数组（与Java端保持一致）
//        byte[] chunkData = new byte[media.chunk.length()];
//        for (int i = 0; i < media.chunk.length(); i++) {
//            chunkData[i] = media.chunk.get_at(i);
//        }
//
//        // 添加调试信息
//        Log.d(TAG, "接收到数据块详情: " +
//                "alertId=" + reportId +
//                ", chunk_seq=" + media.chunk_seq +
//                ", chunk_size=" + chunkData.length +
//                ", total_size=" + media.total_size);
//
//        boolean isComplete = receiver.addChunk(media.chunk_seq, chunkData);
//
//        // 显示进度
//        double progress = receiver.getProgress();
//        Log.i(TAG, String.format("接收进度: alertId=%d, %.1f%% (块 #%d/%d)",
//                reportId, progress * 100, media.chunk_seq + 1, receiver.getTotalChunks()));
//
//        // 如果接收完成，处理完整媒体
//        if (isComplete) {
//            byte[] completeData = receiver.getData();
//            Log.i(TAG, "接收完成，开始处理完整数据，大小: " + completeData.length);
//            handleCompleteMedia(media, completeData);
//            mediaReceivers.remove(alertId); // 清理资源
//        }
//    }
//
//    private void handleCompleteMedia(ReportMedia media, byte[] data) {
//        try {
//            Log.i(TAG, "开始处理完整媒体数据，大小: " + data.length + " 字节");
//
//            // 首先检查数据是否为空或太小
//            if (data == null || data.length == 0) {
//                Log.e(TAG, "接收到的媒体数据为空");
//                return;
//            }
//
//            // 检查JPEG文件头 (JPEG文件通常以0xFFD8开头)
//            if (data.length > 2) {
//                String header = String.format("%02X%02X", data[0], data[1]);
//                Log.i(TAG, "文件头: " + header);
//                if (!"FFD8".equals(header)) {
//                    Log.w(TAG, "数据可能不是有效的JPEG文件");
//                }
//            }
//
//            // 尝试将字节数组转换为Bitmap
//            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//
//            if (bitmap != null) {
//                Log.i(TAG, "Bitmap转换成功，尺寸: " + bitmap.getWidth() + "x" + bitmap.getHeight());
//
//                // 保存到文件系统
//                //saveMediaToFile(media, data);
//
//                // 通知监听器
//                if (mediaListener != null) {
//                    final Bitmap finalBitmap = bitmap;
//                    new android.os.Handler(android.os.Looper.getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//                            mediaListener.onMediaReceived(media.report_id, finalBitmap, media.deviceId, media.deviceType);
//                        }
//                    });
//                }
//            } else {
//                Log.e(TAG, "无法将字节数组转换为Bitmap，数据长度: " + data.length);
//
//                // 尝试使用 BitmapFactory.Options 进行更宽松的解码
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true; // 只获取图片尺寸信息
//                BitmapFactory.decodeByteArray(data, 0, data.length, options);
//                Log.e(TAG, "图片信息 - 宽度: " + options.outWidth + ", 高度: " + options.outHeight +
//                        ", MIME类型: " + options.outMimeType);
//
//                // 将数据保存到文件以便调试
//                //saveRawDataToFile(media, data);
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "处理完整媒体时发生异常", e);
//        } catch (OutOfMemoryError e) {
//            Log.e(TAG, "内存不足，无法解码图片", e);
//        }
//    }
//    public void setOnMediaReceivedListener(OnMediaReceivedListener listener) {
//        this.mediaListener = listener;
//    }
//}
