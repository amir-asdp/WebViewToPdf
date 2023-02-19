package android.print;

import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;

public class PdfFileWriter {

    public static void writePdf(PrintAttributes printAttributes, PrintDocumentAdapter printAdapter,
                                File directory, String fileName, OnPdfWriteListener pdfWriteListener){

        printAdapter.onLayout(null, printAttributes, null, new PrintDocumentAdapter.LayoutResultCallback() {
            public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {
                printAdapter.onWrite(new PageRange[]{PageRange.ALL_PAGES}, getParcelFileDescriptor(directory, fileName), new CancellationSignal(), new PrintDocumentAdapter.WriteResultCallback() {

                    @Override
                    public void onWriteFinished(PageRange[] pages) {
                        super.onWriteFinished(pages);
                        if (pages.length > 0) {
                            pdfWriteListener.onSuccess(new File(directory, fileName).getAbsolutePath());
                        } else {
                            pdfWriteListener.onFailure(OnPdfWriteListener.ZERO_PAGE_FAILURE_MESSAGE);
                        }
                    }

                    @Override
                    public void onWriteFailed(CharSequence error) {
                        super.onWriteFailed(error);
                        pdfWriteListener.onFailure(error.toString());
                    }

                    @Override
                    public void onWriteCancelled() {
                        super.onWriteCancelled();
                        pdfWriteListener.onCancelled();
                    }
                });
            }
        }, null);

    }



    private static ParcelFileDescriptor getParcelFileDescriptor(File directory, String fileName) {

        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, fileName);

        try {
            file.createNewFile();
            return ParcelFileDescriptor.open(file, 805306368);
        }
        catch (Exception e) {
            Log.e(PdfFileWriter.class.getSimpleName(), "Failed to open ParcelFileDescriptor", e);
            return null;
        }

    }



    public interface OnPdfWriteListener{

        String ZERO_PAGE_FAILURE_MESSAGE = "No pages exist.";

        void onSuccess(String pdfFilePath);

        void onFailure(String failureMessage);

        void onCancelled();

    }

}
