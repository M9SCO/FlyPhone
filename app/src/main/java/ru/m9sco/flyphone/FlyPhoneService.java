package ru.m9sco.flyphone;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class FlyPhoneService extends Service {

    PowerManager.WakeLock wakeLock;

    public interface ServiceCallback {
        void onClose();
    }

    private void sendBroadcast (){
        Intent intent = new Intent ("message"); //put the same message as in the filter you used in the activity when registering the receiver
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (FlyPhoneService.class.getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }

        return false;
    }

    private Notification buildNotification(String msg) {
        Notification.Builder notification;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String CHANNELID = "Foreground Service AIS-FlyPhoneCatcher";
            NotificationChannel channel = new NotificationChannel(CHANNELID, CHANNELID,
                    NotificationManager.IMPORTANCE_HIGH);

            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            notification = new Notification.Builder(this, CHANNELID);
        } else {
            notification = new Notification.Builder(this);
        }

        Intent notificationIntent = new Intent(this.getApplicationContext(), MainActivity.class);
        int f = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            f = PendingIntent.FLAG_MUTABLE;
        PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, notificationIntent, f);

        notification.setContentIntent(contentIntent)
                .setContentText(msg)
                .setContentTitle("FlyPhone")
                .setSmallIcon(R.drawable.ic_launcher_foreground);

        return notification.build();
    }


    public void acquireLocks()
    {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
                "AIS-FlyPhoneCatcher:WakeLock");
        wakeLock.acquire();
    }

    public void releaseLocks()
    {
        wakeLock.release();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null) {

            int source = (int) intent.getExtras().get("source");
            int fd = (int) intent.getExtras().get("USB");
            int cgfwide = (int) intent.getExtras().get("CGFWIDE");
            int modeltype = (int) intent.getExtras().get("MODELTYPE");
            int FPDS = (int) intent.getExtras().get("FPDS");

            int r = FlyPhoneCatcher.createReceiver(source, fd, cgfwide, modeltype, FPDS);

            if (r == 0) {
                String msg = "Receiver running - " + DeviceManager.getDeviceTypeDescription() + " @ " + FlyPhoneCatcher.getRateDescription();
                startForeground(1001, buildNotification(msg));

                new Thread(
                        () -> {
                            acquireLocks();

                            FlyPhoneCatcher.Run();
                            FlyPhoneCatcher.Close();

                            stopForeground(true);
                            stopSelf();
                            sendBroadcast();

                            releaseLocks();
                        }).start();
            } else {
                String msg = "Receiver creation failed";
                startForeground(1001, buildNotification(msg));

                stopForeground(true);
                stopSelf();
                sendBroadcast();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}