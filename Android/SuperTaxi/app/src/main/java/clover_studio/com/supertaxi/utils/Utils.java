package clover_studio.com.supertaxi.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import clover_studio.com.supertaxi.file.LocalFilesManagement;
import clover_studio.com.supertaxi.models.ImageAvatarModel;
import clover_studio.com.supertaxi.singletons.UserSingleton;

/**
 * Created by ubuntu_ivo on 08.02.16..
 */
public class Utils {

    /**
     * checked if android version is above given version
     * @param version version to compare
     * @return true is above, false is equals or below
     */
    public static boolean isBuildOver(int version) {
        if (android.os.Build.VERSION.SDK_INT > version)
            return true;
        return false;
    }

    public static boolean isBuildBelow(int version) {
        if (android.os.Build.VERSION.SDK_INT < version)
            return true;
        return false;
    }

    public static void hideKeyboard(View pView, Activity pActivity) {
        if (pView == null) {
            pView = pActivity.getWindow().getCurrentFocus();
        }
        if (pView != null) {
            InputMethodManager imm = (InputMethodManager) pActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(pView.getWindowToken(), 0);
            }
        }
    }

    /**
     * return string of given size example 1MB, 1,4GB, 300KB
     * @param size size to convert
     * @return string
     */
    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String getImagePathImageProfile(Context cntx, Uri uri, boolean isOverJellyBeam) {

        if (isOverJellyBeam) {
            try {
                ParcelFileDescriptor parcelFileDescriptor = cntx.getContentResolver().openFileDescriptor(uri, "r");

                if (parcelFileDescriptor != null) {

                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    // Bitmap image =
                    // BitmapFactory.decodeFileDescriptor(fileDescriptor);
                    copyStream(new FileInputStream(fileDescriptor), new FileOutputStream(new File(LocalFilesManagement.getProfileImagesFolder() + "/" + "image_profile")));
                    parcelFileDescriptor.close();
                    // saveBitmapToFile(image, cntx.getExternalCacheDir() + "/" +
                    // "image_profile");

                    return LocalFilesManagement.getProfileImagesFolder() + "/" + "image_profile";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = cntx.getContentResolver().query(uri, projection, null, null, null);

            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();

                String columnIndex = cursor.getString(column_index);
                cursor.close();

                return columnIndex;
            }
        }

        return "";
    }

    public static void copyStream(InputStream is, OutputStream os) {

        final int buffer_size = 1024;
        try {

            byte[] bytes = new byte[buffer_size];
            while (true) {
                // Read byte from input stream

                int count = is.read(bytes, 0, buffer_size);
                if (count == -1) {
                    break;
                }

                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static boolean checkGrantResults (int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static String getMyAvatarUrl(){
        ImageAvatarModel avatar = UserSingleton.getInstance().getUser().avatar;
        if(avatar != null){
            if(TextUtils.isEmpty(avatar.fileid) && TextUtils.isEmpty(avatar.thumbfileid)){
                return "";
            }else if(!TextUtils.isEmpty(avatar.thumbfileid)){
                return Const.BASE_URL + Const.Server.UPLOADS + "/" + avatar.thumbfileid;
            }else if(!TextUtils.isEmpty(avatar.fileid)){
                return Const.BASE_URL + Const.Server.UPLOADS + "/" + avatar.fileid;
            }
        }
        return "";
    }

    public static String formatAddress(Address address){
        StringBuilder returnString = new StringBuilder();
        returnString.append(address.getAddressLine(0));
        if(!TextUtils.isEmpty(address.getPostalCode())){
            returnString.append(", " + address.getPostalCode());
        }if(!TextUtils.isEmpty(address.getLocality())){
            returnString.append(", " + address.getLocality());
        }
        return returnString.toString();
    }

}
