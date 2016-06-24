package com.honey.aaron.workoff;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;

public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();
    private Thread.UncaughtExceptionHandler mUncaughtExceptionHandler;

    private static MyApplication myApplication;

    @Override
    public void onCreate() {
        mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandlerApplication());

        super.onCreate();
        myApplication = this;
    }

    public static MyApplication getInstance() {
        synchronized (myApplication) {
            return myApplication;
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.w(TAG, TAG + " terminated!!");
        myApplication = null;
    }

    /**
     * 메시지로 변환
     * @param th Throwable
     * @return String Log
     */
    private String getStackTrace(Throwable th) {

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        Throwable cause = th;
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }

    private boolean makeLogFile(String logText) {
        String dirPath = getFilesDir().getAbsolutePath();
        File file = new File(dirPath);

        boolean isDirectoryCreated = file.exists();
        // 일치하는 폴더가 없으면 생성
        if (!isDirectoryCreated) {
            isDirectoryCreated = file.mkdirs();
            Log.d(TAG, "Log 파일을 저장할 폴더 생성중...");
        }

        if(!isDirectoryCreated) {
            Toast.makeText(this, "폴더를 생성할 수 없습니다.\n관리자에게 문의하십시오.", Toast.LENGTH_SHORT).show();
            return false;
        }

        Calendar cal = Calendar.getInstance();
        // txt 파일 생성
        File saveFile = new File(dirPath + "/log" + "_" + cal.get(Calendar.YEAR) + cal.get(Calendar.MONTH) + cal.get(Calendar.DAY_OF_MONTH) +
                "_" + cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) + cal.get(Calendar.SECOND) + ".txt");
        try {
            FileOutputStream fos = new FileOutputStream(saveFile);
            fos.write(logText.getBytes());
            fos.flush();
            fos.close();
            Log.d(TAG, "Log 파일 생성중...");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "파일을 생성할 수 없습니다.\n관리자에게 문의하십시오.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 파일이 1개 이상이면 파일 이름 출력
//        if (file.listFiles().length > 0) {
//            for (File f : file.listFiles()) {
//                String str = f.getName();
//                Log.v(TAG, "fileName : " + str);
//
//                // 파일 내용 읽어오기
//                String loadPath = dirPath + "/" + str;
//                try {
//                    FileInputStream fis = new FileInputStream(loadPath);
//                    BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fis));
//
//                    String content = "", temp;
//                    while ((temp = bufferReader.readLine()) != null) {
//                        content += temp;
//                    }
//                    Log.v(TAG, content);
//                    fis.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return false;
//                }
//            }
//        }

        return true;
    }

    class UncaughtExceptionHandlerApplication implements Thread.UncaughtExceptionHandler{

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e(TAG, getStackTrace(ex));
            if(!makeLogFile(getStackTrace(ex))) mUncaughtExceptionHandler.uncaughtException(thread, ex);
        }
    }
}
