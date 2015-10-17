package com.floatlearning.fileio;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Tools used to save data to disk, especially image data.
 */
public class FileIO {
    /**
     * String representation of the environment variable for the external storage path.
     */
    private static final String EXTERNAL_STORAGE = "EXTERNAL_STORAGE";
    /**
     * Path to external storage; this is the most reliable method found to get the external storage path.
     */
    private static final String STORAGE_PATH = System.getenv(EXTERNAL_STORAGE);
    /**
     * The path separator on this system.
     */
    private static final String PATH_SEPARATOR = "/";
    /**
     * The quality to use when saving to JPEG.
     */
    private static final int JPEG_QUALITY = 90;
    /**
     * Extension for JPEG files.
     */
    public static final String EXT_JPEG = "jpg";

    public static final String EXT_PNG = "png";

    /**
     * Converts raw camera preview bytes into a JPEG-compressed byte array and then saves that to disk.
     *
     * @param bytes            The camera preview bytes.
     * @param width            The width of the camera preview.
     * @param height           The height of the camera preview.
     * @param previewFormat    The ImageFormat of the camera preview.
     * @param folder           The folder to which to save the image.
     * @param filename         The filename to use when saving the image; the extension will be `.jpg`
     * @return  True if successful, false otherwise.
     */
    public static boolean saveImageFromPreviewBytes(final byte[] bytes, final int width, final int height, final int previewFormat, final String folder, final String filename) {
        if (bytes == null || width < 1 || height < 1) {
            // unable to continue due to invalid parameters
            return false;
        }

        YuvImage image = new YuvImage(bytes, previewFormat, width, height, null);
        Rect imageRect = new Rect(0, 0, width, height);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        image.compressToJpeg(imageRect, JPEG_QUALITY, output);

        return saveBytes(output.toByteArray(), folder, filename, EXT_JPEG);
    }

    /**
     * Save a bitmap to file, with a timestamp appended to the filename.
     *
     * @param bitmap    The bitmap to save to file.
     * @param folder    The folder to save to.
     * @param prefix    The prefix portion of the file name. The rest of the file name will be the current time and date.
     * @param png       Whether or not to use PNG (the alternative is JPEG).
     * @return  True if the save was successful, false otherwise.
     */
    public static boolean saveImageFromBitmapWithTimestamp(final Bitmap bitmap, final String folder, final String prefix, final boolean png) {
        return saveImageFromBitmap(bitmap, folder, prefix + new Date().toString().replace(" ", "_"), png);
    }

    /**
     * Save a bitmap to file, with a unique index appended to the filename.
     *
     * @param bitmap    The bitmap to save to file.
     * @param folder    The folder to save to.
     * @param prefix    The prefix portion of the file name. The rest of the file name will be the next available padded number.
     * @param png       Whether or not to use PNG (the alternative is JPEG).
     * @return  True if the save was successful, false otherwise.
     */
    public static boolean saveImageFromBitmapWithUniqueSuffix(final Bitmap bitmap, final String folder, final String prefix, final boolean png) {
        String folderPath = STORAGE_PATH + PATH_SEPARATOR + folder;
        File targetDirectory = new File(folderPath);

        if (!targetDirectory.exists()) {
            if (!targetDirectory.mkdirs()) {
                // unable to continue; cannot create requested folder
                return false;
            }
        }

        File[] files = targetDirectory.listFiles();
        int index = 0;

        for (File file : files) {
            if (file.getName().substring(0, prefix.length()).equals(prefix)) {
                index++;
            }
        }

        return saveImageFromBitmap(bitmap, folder, prefix + pad(index), png);
    }

    /**
     * Save a bitmap to file.
     *
     * @param bitmap      The bitmap to save to file.
     * @param folder      The folder to save to.
     * @param filename    The name of the file.
     * @param png         Whether or not to use PNG (the alternative is JPEG).
     * @return  True if the save was successful, false otherwise.
     */
    public static boolean saveImageFromBitmap(final Bitmap bitmap, final String folder, final String filename, final boolean png) {
        String folderPath = STORAGE_PATH + PATH_SEPARATOR + folder;
        File targetDirectory = new File(folderPath);

        if (!targetDirectory.exists()) {
            if (!targetDirectory.mkdirs()) {
                // unable to continue; cannot create requested folder
                return false;
            }
        }

        File file = new File(folderPath + PATH_SEPARATOR + filename + "." + (png ? EXT_PNG : EXT_JPEG));
        FileOutputStream output;

        try {
            output = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            // unable to continue; tried to create destination file but was unable to do so
            return false;
        }

        bitmap.compress(png ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output);

        try {
            output.close();
        } catch (IOException e) {
            // unable to continue; could not write byte array to file
            return false;
        }

        return true;
    }

    /**
     * Save an arbitrary byte array to a given folder with the provided filename.
     *
     * @param bytes       The data to save to disk.
     * @param folder      The folder to store this file in.
     * @param filename    The name of the file when saved.
     * @param extension   The extension of the file to be created.
     * @return  True if successful, false otherwise.
     */
    public static boolean saveBytes(final byte[] bytes, final String folder, final String filename, final String extension) {
        if (bytes == null || folder.isEmpty() || filename.isEmpty()) {
            // unable to continue due to invalid parameters
            return false;
        }

        String folderPath = STORAGE_PATH + PATH_SEPARATOR + folder;
        File targetDirectory = new File(folderPath);

        if (!targetDirectory.exists()) {
            if (!targetDirectory.mkdirs()) {
                // unable to continue; cannot create requested folder
                return false;
            }
        }

        File file = new File(folderPath + PATH_SEPARATOR + filename + "." + extension);
        FileOutputStream output;

        try {
            output = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            // unable to continue; tried to create destination file but was unable to do so
            return false;
        }

        try {
            output.write(bytes);
            output.close();
        } catch (IOException e) {
            // unable to continue; could not write byte array to file
            return false;
        }

        return true;
    }

    private static String pad(final int number) {
        if (number < 10) {
            return "000" + number;
        } else if (number < 100) {
            return "00" + number;
        } else if (number < 1000) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }
}
