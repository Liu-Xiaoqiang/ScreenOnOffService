package com.chipsee.screenonoffservice;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

public class ScreenOnOffService extends Service {

    private static final String TAG = "ScreenOnOffService";

    private IntentFilter intentFilter;
    private StorageBroadcastReceiver mStorageBroadcastReceiver;

    private static final String SCREEN_ON = "android.intent.action.SCREEN_ON";
    private static final String SCREEN_OFF = "android.intent.action.SCREEN_OFF";

    //If the storage is health
    String EXT_STORAGE_FILE_PATH = "";
    String EXT_STORAGE_FILE_NAME = "tmp.log";
    String EXT_STORAGE_FILE_CONTENTS = "EXT Storage file contents, don't remove it";
    boolean isExtSDHealth = true;
    private static final int TRYNUM = 10000;

    //ExtSD Infor
    String volId;
    public static final int STATE_UNMOUNTED = 0;
    public static final int STATE_CHECKING = 1;
    public static final int STATE_MOUNTED = 2;
    public static final int STATE_MOUNTED_READ_ONLY = 3;
    public static final int STATE_FORMATTING = 4;
    public static final int STATE_EJECTING = 5;
    public static final int STATE_UNMOUNTABLE = 6;
    public static final int STATE_REMOVED = 7;
    public static final int STATE_BAD_REMOVAL = 8;


    public ScreenOnOffService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //addAction and registerReceiver
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        mStorageBroadcastReceiver = new StorageBroadcastReceiver();
        registerReceiver(mStorageBroadcastReceiver, intentFilter);

        //VolumeId
        volId = getPublicVolumeId(this);

        Log.d(TAG, "onCreate: Service created, Version (V1.1)");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runTask();
                Log.d(TAG, "onStartCommand run: Service is running");
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    class StorageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (SCREEN_ON.equals(intent.getAction())){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runTask();
                    }
                }).start();
                Log.d(TAG, "***SCREEN_ON***");
            } else if(SCREEN_OFF.equals(intent.getAction())){
                Log.d(TAG, "***SCREEN_OFF***");
            }
        }
    }

    public void runTask(){
//        if(!isExtSDHealth() && isMounted() ) {
        if(isMounted()) {
            unmountandmount();
            Log.d(TAG, "run: ==============UNMOUNT-AND-MOUNT=============");
        } else if (isUnMounted()){
            mount();
            Log.d(TAG, "run: ==============MOUNT=============");
        } else{
            Log.d(TAG, "run: ExtSD is Health, Execute Nothing");
        }
    }

    public boolean isExtSDHealth() {
        isExtSDHealth = false;
        int count=TRYNUM;
        String isf = "0";
        boolean ret = false;
        ArrayList paths = (ArrayList) getExtSDCardPath(this);
        for(int i = 0;i < paths.size(); i ++){
            Log.d(TAG, "onCreate: path"+i+"="+paths.get(i));
            EXT_STORAGE_FILE_PATH = (String) paths.get(i);
            while( count > 0 ){
                FileUtils.writeToFile(EXT_STORAGE_FILE_PATH + File.separator + EXT_STORAGE_FILE_NAME, EXT_STORAGE_FILE_CONTENTS);
                isf = FileUtils.readFile(EXT_STORAGE_FILE_PATH + File.separator + EXT_STORAGE_FILE_NAME);
                ret = FileUtils.delete(EXT_STORAGE_FILE_PATH + File.separator + EXT_STORAGE_FILE_NAME);
                if(!ret || !isf.equals(EXT_STORAGE_FILE_CONTENTS)){
                    isExtSDHealth = false;
                    return isExtSDHealth;
                }
                count -= 1;
            }
//            Log.d(TAG, "onCreate: EXT Storage file is " + EXT_STORAGE_FILE_PATH + File.separator + EXT_STORAGE_FILE_NAME);
            Log.d(TAG, "onCreate: EXT Storage file contents is " + "[" + isf + "]");
            if(isf.equals(EXT_STORAGE_FILE_CONTENTS) && ret){
                isExtSDHealth = true;
            }else{
                isExtSDHealth = false;
            }
        }
        return isExtSDHealth;
    }

    public boolean isMounted(){
        if(getPublicVolumeState(this) == STATE_MOUNTED)
            return true;
        else
            return false;
    }

    public boolean isUnMounted(){
        if(getPublicVolumeState(this) == STATE_UNMOUNTED)
            return true;
        else
            return false;
    }

    public static int getPublicVolumeState(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context
                .STORAGE_SERVICE);
        String volId = new String();
        int state = STATE_UNMOUNTED ;
        try {
            Class storeManagerClazz = Class.forName("android.os.storage.StorageManager");
            Method getVolumesMethod = storeManagerClazz.getMethod("getVolumes");
            List<?> volumeInfos  = (List<?>)getVolumesMethod.invoke(storageManager);

            Class volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            Method getIdMethod = volumeInfoClazz.getMethod("getId");
            Method getStateMethod = volumeInfoClazz.getMethod("getState");

            if(volumeInfos != null){
                for(Object volumeInfo:volumeInfos){
                    volId = (String)getIdMethod.invoke(volumeInfo);
                    state = (int) getStateMethod.invoke(volumeInfo);
                    if(volId.contains("public")){
                        break;
                    }else {
                        volId = null;
                        state = STATE_UNMOUNTED;
                    }
                }
            }
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        Log.d(TAG, "getPublicVolumeState: "+ String.valueOf(state) + " volId :" + volId);
        return state;
    }

    public static String getPublicVolumeId(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context
                .STORAGE_SERVICE);
        String volId = new String();
        try {
            Class storeManagerClazz = Class.forName("android.os.storage.StorageManager");
            Method getVolumesMethod = storeManagerClazz.getMethod("getVolumes");
            List<?> volumeInfos  = (List<?>)getVolumesMethod.invoke(storageManager);

            Class volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            Method getIdMethod = volumeInfoClazz.getMethod("getId");
            if(volumeInfos != null){
                for(Object volumeInfo:volumeInfos){
                    volId = (String)getIdMethod.invoke(volumeInfo);
                    if(volId.contains("public")){
                        break;
                    } else {
                        volId = null;
                    }
                }
            }
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        Log.d(TAG, "getPublicVolumeId: " + volId);
        return volId;
    }

    public static List getExtSDCardPath(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context
                .STORAGE_SERVICE);
        ArrayList paths = new ArrayList();
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            Class<?>[] paramClasses = {};
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", paramClasses);
            getVolumePathsMethod.setAccessible(true);
            Object[] params = {};
            Object invoke = getVolumePathsMethod.invoke(storageManager, params);
            paths = new ArrayList(Arrays.asList((String[]) invoke));
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        if (paths.contains(rootPath))
            paths.remove(rootPath);

        return paths;
    }

    public static class UnmountTask extends AsyncTask<Void, Void, Exception> {
        private final Context mContext;
        private final StorageManager mStorageManager;
        private final String mVolumeId;

        public UnmountTask(Context context, String volId) {
            mContext = context.getApplicationContext();
            mStorageManager = (StorageManager)mContext.getSystemService(Context.STORAGE_SERVICE);
            mVolumeId = volId;
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Class<?>[] paramClasses = {String.class};
                Method mUnMountMethod = StorageManager.class.getMethod("unmount", paramClasses);
                mUnMountMethod.setAccessible(true);
                Object[] par = {mVolumeId};
                Object invoke = mUnMountMethod.invoke(mStorageManager, par);
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception e) {
            if (e == null) {
                Log.d(TAG, "unmount "+ mVolumeId + " scucessfully");
            } else {
                Log.e(TAG, "Failed to unmount " + mVolumeId, e);
            }
        }
    }

    public static class UnmountAndMountTask extends AsyncTask<Void, Void, Exception> {
        private final Context mContext;
        private final StorageManager mStorageManager;
        private final String mVolumeId;

        public UnmountAndMountTask(Context context, String volId) {
            mContext = context.getApplicationContext();
            mStorageManager = (StorageManager)mContext.getSystemService(Context.STORAGE_SERVICE);
            mVolumeId = volId;
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Class<?>[] paramClasses = {String.class};
                Method mUnMountMethod = StorageManager.class.getMethod("unmount", paramClasses);
                Method MountMethod = StorageManager.class.getMethod("mount", paramClasses);
                mUnMountMethod.setAccessible(true);
                MountMethod.setAccessible(true);
                Object[] par = {mVolumeId};
                Object invoke = mUnMountMethod.invoke(mStorageManager, par);
                sleep(1000);
                Object invoke2 = MountMethod.invoke(mStorageManager, par);
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception e) {
            if (e == null) {
                Log.d(TAG, "unmountANDmount "+ mVolumeId + " scucessfully");
            } else {
                Log.e(TAG, "Failed to unmount " + mVolumeId, e);
            }
        }
    }

    public static class MountTask extends AsyncTask<Void, Void, Exception> {
        private final Context mContext;
        private final StorageManager mStorageManager;
        private final String mVolumeId;

        public MountTask(Context context, String volId) {
            mContext = context.getApplicationContext();
            mStorageManager = (StorageManager)mContext.getSystemService(Context.STORAGE_SERVICE);
            mVolumeId = volId;
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Class<?>[] paramClasses = {String.class};
                Method mUnMountMethod = StorageManager.class.getMethod("mount", paramClasses);
                mUnMountMethod.setAccessible(true);
                Object[] par = {mVolumeId};
                Object invoke = mUnMountMethod.invoke(mStorageManager, par);
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception e) {
            if (e == null) {
                Log.d(TAG, "mount "+ mVolumeId + " scucessfully");
            } else {
                Log.e(TAG, "Failed to mount " + mVolumeId, e);
            }
        }
    }

    public void unmountandmount(){
        new UnmountAndMountTask(this,volId).execute();
    }

    public void unmount(){
        new UnmountTask(this,volId).execute();
    }

    public void mount(){
        new MountTask(this,volId).execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mStorageBroadcastReceiver);
    }

}
