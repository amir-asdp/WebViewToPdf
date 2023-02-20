# Info
**This library is supported by API 19 (Android 4.4) to API 33 (Android 13) and it has the following features :**

- Create a PDF file from a WebView.
- Save the created PDF file in an arbitary directory.
- Open a PDF file using chooser.
- Handles all required permissions according to the Android OS Version.
- Build and show ***Go Settings Rational Dialog*** for denied permission scenario. 

### 
### 
### 
# Gradle Configuration
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

```
dependencies {
	        implementation 'com.github.amir-asdp:WebViewToPdf:1.0.0'
	}
```

### 
### 
### 
# How To Use
**Add following codes in AndroidManifest.xml** :
```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    /...
    
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"/>
    

    <application        
        android:requestLegacyExternalStorage="true"
        
        /...

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="com.package.name.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/provider_paths" />
        </provider>
    </application>

</manifest>
```

### 
**Create a `provider_paths.xml` file in `res/xml` folder.** :
```
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">

    <external-path
        name="external_files"
        path="."/>

</paths>
```

### 
**SAMPLE CODE :**

Use `convertWebViewToPdf(..)` to save a WebView object as a PDF file in your arbitary directory. Use `openPdfFile(..)` to open a pdf file using chooser.
Also you can use `buildPdfPermissionsRationalDialog(..)` when the permissions are denied.**
```
File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/MyExapmleApp/");
String fileName = "Exapmle.pdf";


//Receive webView in "onFinished(WebView webView, String url)" callback and pass it to the following method.

WebViewToPdf.convertWebViewToPdf(mContext, webView, directory, fileName, new WebViewToPdf.OnConvertResultListener() {

            @Override
            public void onSuccess(String pdfFilePath) {
                WebViewToPdf.openPdfFile(mContext, pdfFilePath, new WebViewToPdf.OnOpenResultListener() {
                    @Override
                    public void onPermissionDenied() {
                        WebViewToPdf.buildPdfPermissionsRationalDialog(mContext, WebViewToPdf.PERMISSION_DIALOG_ENGLISH).show();
                    }
                });
            }
            @Override
            public void onFailure(String failMessage) {
                Toast.makeText(mContext, failMessage, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled() {
                Toast.makeText(mContext, "Convert Cancelled", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onPermissionDenied() {
                WebViewToPdf.buildPdfPermissionsRationalDialog(mContext, WebViewToPdf.PERMISSION_DIALOG_ENGLISH).show();
            }
            
        });
```
