package com.chipsee.screenonoffservice;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Chipsee
 * on 2019/06/28
 */
public class FileUtils {
    private static final String TAG = "ScreenOnOffService";

    public static String readRawFile(Context context,int Rid) {
        StringBuilder stringBuilder = new StringBuilder();
        char [] buf = new char[64];
        int count=0;
        try {
            InputStream in = context.getResources().openRawResource(Rid);
            InputStreamReader reader = new InputStreamReader(in, "UTF-8");
            while ((count = reader.read(buf)) != -1) {
                stringBuilder.append(buf,0,count);
            }
        } catch (Exception e) {
            Log.e(TAG, "FileUtils: " + e.getMessage());
        }
        return stringBuilder.toString();
    }

    public static String readAssetsFile(Context context,String filePath) {
        StringBuilder stringBuilder = new StringBuilder();
        char [] buf = new char[64];
        int count=0;
        try {
            InputStream in = context.getAssets().open(filePath);
            InputStreamReader reader = new InputStreamReader(in, "UTF-8");
            while ((count = reader.read(buf)) != -1) {
                stringBuilder.append(buf,0,count);
            }
        } catch (Exception e) {
            Log.e(TAG, "FileUtils: " + e.getMessage());
        }
        return stringBuilder.toString();
    }

    public static String readFile(String filePath) {
        File file = new File(filePath);
        StringBuilder stringBuilder = new StringBuilder();
        char [] buf = new char[64];
        int count=0;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(fileInputStream, "UTF-8");
            while ((count = reader.read(buf)) != -1) {
                stringBuilder.append(buf,0,count);
            }
        } catch (Exception e) {
            Log.e(TAG, "FileUtils: " + e.getMessage());
        }
        return stringBuilder.toString();
    }

    public static void writeToFile(String filePath,String content){
        File file = getFile(filePath);
        try {
            FileWriter fw = new FileWriter(file,false);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
            fw.close();
        } catch (Exception e) {
            Log.e(TAG, "FileUtils: " + e.getMessage());
        }
    }

    public static File getFile(String filePath) {
        File dir = new File(filePath);
        if (!dir.getParentFile().exists()) {
            dir.getParentFile().mkdirs();
        }
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                boolean flag = file.createNewFile();
                if (!flag) {
                    Log.e(TAG, "FileUtils: createNewFile Fail");
                }
            } catch (Exception e) {
                Log.e(TAG, "FileUtils: " + e.getMessage());
            }
        }
        return file;
    }



    /** 删除文件，可以是文件或文件夹
     * @param delFile 要删除的文件夹或文件名
     * @return 删除成功返回true，否则返回false
     */
    public static boolean delete(String delFile) {
        File file = new File(delFile);
        if (!file.exists()) {
            Log.e(TAG, "FileUtils: delete "+ delFile + " not exists.");
            return false;
        } else {
            if (file.isFile())
                return deleteSingleFile(delFile);
            else
                return deleteDirectory(delFile);
        }
    }

    /** 删除单个文件
     * @param filePath$Name 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
//                Log.d(TAG, "FileUtils: deleteSingleFile: " +filePath$Name + "successfully");
                return true;
            } else {
                Log.e(TAG, "FileUtils: deleteSingleFile: "+ filePath$Name + " fail." );
                return false;
            }
        } else {
            Log.e(TAG, "FileUtils: deleteSingleFile: "+ filePath$Name + " not exists" );
            return false;
        }
    }

    /** 删除目录及目录下的文件
     * @param filePath 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator))
            filePath = filePath + File.separator;
        File dirFile = new File(filePath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            Log.e(TAG, "FileUtils: deleteDirectory: " + filePath + " not exists." );
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (File file : files) {
            // 删除子文件
            if (file.isFile()) {
                flag = deleteSingleFile(file.getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (file.isDirectory()) {
                flag = deleteDirectory(file
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            Log.e(TAG, "FileUtils: deleteDirectory: fail.");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            Log.d(TAG, "FileUtils: deleteDirectory: " + filePath + " successfully");
            return true;
        } else {
            Log.e(TAG, "deleteDirectory: "+ filePath + " fail." );
            return false;
        }
    }











}
