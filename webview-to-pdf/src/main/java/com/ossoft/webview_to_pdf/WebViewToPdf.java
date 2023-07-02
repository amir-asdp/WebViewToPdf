package com.ossoft.webview_to_pdf;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.print.PdfFileWriter;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.provider.Settings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;

public class WebViewToPdf {

    public static final int DIALOG_LANG_ENGLISH = 1;
    public static final int DIALOG_LANG_FARSI = 2;

    public static void convertWebViewToPdf(@NonNull Context context, @NonNull WebView webView, @NonNull File destinationDirectory,
                                           @NonNull String fileName, @NonNull OnConvertResultListener convertResultListener){

        checkStoragePermissions(context, new OnPermissionResultListener() {
            @Override
            public void onResult(boolean areAllPermissionsGranted) {
                if (areAllPermissionsGranted){
                    PrintAttributes printAttributes = new PrintAttributes.Builder()
                            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                            .build();
                    PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(fileName);
                    PdfFileWriter.writePdf(printAttributes, printAdapter, destinationDirectory, fileName, new PdfFileWriter.OnPdfWriteListener() {
                        @Override
                        public void onSuccess(String pdfFilePath) {
                            convertResultListener.onSuccess(pdfFilePath);
                        }

                        @Override
                        public void onFailure(String failureMessage) {
                            convertResultListener.onFailure(failureMessage);
                        }

                        @Override
                        public void onCancelled() {
                            convertResultListener.onCancelled();
                        }
                    });
                }
                else {
                    convertResultListener.onPermissionDenied();
                }
            }
        });

    }

    public static void openPdfFile(@NonNull Context context, @NonNull String path, @NonNull OnOpenResultListener openResultListener){
        checkStoragePermissions(context, new OnPermissionResultListener() {
            @Override
            public void onResult(boolean areAllPermissionsGranted) {
                if (areAllPermissionsGranted){
                    File pdfFile = new File(path);

                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                        uri = FileProvider.getUriForFile(context, "com.package.name.fileprovider", pdfFile);
                    }
                    else {
                        uri = Uri.fromFile(pdfFile);
                    }


                    Intent target = new Intent(Intent.ACTION_VIEW);
                    target.setDataAndType(uri, "application/pdf");
                    target.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Intent chooser = Intent.createChooser(target, "Open File");
                    context.startActivity(chooser);
                }
                else {
                    openResultListener.onPermissionDenied();
                }
            }
        });
    }

    public static void checkStoragePermissions(@NonNull Context context, @NonNull OnPermissionResultListener resultListener){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            resultListener.onResult(Environment.isExternalStorageManager());
        }
        else {
            Dexter.withContext(context)
                    .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                            resultListener.onResult(multiplePermissionsReport.areAllPermissionsGranted());
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                            permissionToken.cancelPermissionRequest();
                            resultListener.onResult(false);
                        }
                    })
                    .onSameThread()
                    .check();
        }

    }

    public static AlertDialog buildPdfPermissionsRationalDialog(@NonNull Context context){

        return buildPdfPermissionsRationalDialog(context, DIALOG_LANG_ENGLISH);

    }

    public static AlertDialog buildPdfPermissionsRationalDialog(@NonNull Context context, int dialogLanguageId){

        return buildPdfPermissionsRationalDialog(context, dialogLanguageId, 0);

    }

    public static AlertDialog buildPdfPermissionsRationalDialog(@NonNull Context context, int dialogLanguageId, int overrideThemeResId){

        String[] dialogStrings;
        if (dialogLanguageId == DIALOG_LANG_FARSI) {
            dialogStrings = context.getResources().getStringArray(R.array.farsi_permission_rational_dialog);
        }
        else {
            dialogStrings = context.getResources().getStringArray(R.array.english_permission_rational_dialog);
        }

        return buildPdfPermissionsRationalDialog(context, dialogStrings[0], dialogStrings[1], dialogStrings[2], dialogStrings[3], overrideThemeResId);

    }

    public static AlertDialog buildPdfPermissionsRationalDialog(@NonNull Context context,
                                                                @NonNull String dialogTitle,
                                                                @NonNull String dialogMessage,
                                                                @NonNull String dialogGoSettingsButton,
                                                                @NonNull String dialogCancelButton,
                                                                int overrideThemeResId){

        DialogInterface.OnClickListener rationalDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                        context.startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + context.getPackageName())));
                    }
                    else {
                        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                        context.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(uri));
                    }
                }
                dialog.cancel();
            }
        };

        return new MaterialAlertDialogBuilder(context, overrideThemeResId)
                .setIcon(R.drawable.ic_permission_media)
                .setTitle(dialogTitle)
                .setMessage(dialogMessage)
                .setPositiveButton(dialogGoSettingsButton, rationalDialogClickListener)
                .setNegativeButton(dialogCancelButton, rationalDialogClickListener)
                .create();

    }



    public interface OnConvertResultListener{

        void onSuccess(String pdfFilePath);

        void onFailure(String failMessage);

        void onCancelled();

        void onPermissionDenied();

    }

    public interface OnOpenResultListener{

        void onPermissionDenied();

    }

    public interface OnPermissionResultListener{

        void onResult(boolean areAllPermissionsGranted);

    }

}
