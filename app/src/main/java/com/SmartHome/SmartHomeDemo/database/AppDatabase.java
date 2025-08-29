package com.SmartHome.SmartHomeDemo.database;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Device.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    //
    //存储已配对设备, 当一个设备上线时, 检查这里设备列表, 若不存在, 则进行配对
    //
    private static AppDatabase INSTANCE;

    public abstract DeviceDao deviceDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "smart_home_database")
                            .allowMainThreadQueries() // 仅用于测试，实际应用中应避免在主线程执行数据库操作
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
