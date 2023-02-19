package com.ossoft.webview_to_pdf;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.print.PdfFileWriter;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.provider.Settings;
import android.webkit.WebView;

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

    public static final String PERMISSION_DIALOG_FARSI = "fa";
    public static final String PERMISSION_DIALOG_ENGLISH = "en";

    public static void convertWebViewToPdf(Context context, WebView webView, File directory,
                                           String fileName, OnConvertResultListener convertResultListener){

        checkPdfPermissions(context, new OnPermissionResultListener() {
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
                    PdfFileWriter.writePdf(printAttributes, printAdapter, directory, fileName, new PdfFileWriter.OnPdfWriteListener() {
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

    public static void openPdfFile(Context context, String path, OnOpenResultListener openResultListener){
        checkPdfPermissions(context, new OnPermissionResultListener() {
            @Override
            public void onResult(boolean areAllPermissionsGranted) {
                if (areAllPermissionsGranted){
                    File pdfFile = new File(path);

                    Uri uri = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                        uri = FileProvider.getUriForFile(context, "com.package.name.fileprovider", pdfFile);
                    }
                    else {
                        Uri pdfUri = Uri.fromFile(pdfFile);
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

    public static void checkPdfPermissions(Context context, OnPermissionResultListener resultListener){

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

    public static AlertDialog buildPdfPermissionsRationalDialog(Context context, String language){

        String[] dialogStrings = context.getResources().getStringArray(R.array.english_permission_rational_dialog);
        if (language.equals(PERMISSION_DIALOG_FARSI)){
            dialogStrings = context.getResources().getStringArray(R.array.farsi_permission_rational_dialog);
        }

        DialogInterface.OnClickListener rationalDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                        context.startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
                    }
                    else {
                        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                        context.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(uri));
                    }
                }
                dialog.cancel();
            }
        };

        return new MaterialAlertDialogBuilder(context, R.style.AlertDialogStyle)
                .setIcon(R.drawable.ic_permission_media)
                .setTitle(dialogStrings[0])
                .setMessage(dialogStrings[1])
                .setPositiveButton(dialogStrings[2], rationalDialogClickListener)
                .setNegativeButton(dialogStrings[3], rationalDialogClickListener)
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
