package uk.co.iseeshapes.capture.controller.listener;

import org.indilib.i4j.client.INDIBLOBElement;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDIElementListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class CCDDownloadListener implements INDIElementListener {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(CCDDownloadListener.class);

    private INDIBLOBElement downloadElement;
    private PrintStream out;
    private File file;
    private int lineLength;
    private int numberOfFrames;
    private int imageNumber;

    private boolean complete;
    private boolean displayFrames;

    private CCDDownloadListener(INDIBLOBElement downloadElement, PrintStream out, File file, int lineLength,
                                int numberOfFrames, int imageNumber, boolean displayFrames) {
        this.downloadElement = downloadElement;
        this.out = out;
        this.file = file;
        this.lineLength = lineLength;
        this.numberOfFrames = numberOfFrames;
        this.imageNumber = imageNumber;
        this.displayFrames = displayFrames;

        complete = false;

        downloadElement.addINDIElementListener(this);
    }

    public CCDDownloadListener(INDIBLOBElement downloadElement, PrintStream out, File file, int lineLength,
                               int numberOfFrames, int imageNumber) {
        this (downloadElement, out, file, lineLength, numberOfFrames, imageNumber, true);
    }

    public CCDDownloadListener(INDIBLOBElement downloadElement, PrintStream out, File file, int lineLength) {
        this (downloadElement, out, file, lineLength, 0, 1, false);
    }

    public CCDDownloadListener(INDIBLOBElement downloadElement, File file) {
        this (downloadElement, null, file, 1, 0, 1, false);
    }

    public boolean isComplete() {
        return complete;
    }

    @Override
    public void elementChanged(INDIElement indiElement) {
        if (!downloadElement.equals(indiElement)) {
            return;
        }

        try {
            downloadElement.getValue().saveBLOBData(file);
        } catch (IOException e) {
            out.print('\n');
            log.error("Unable to download file", e);
            complete = true;
            return;
        }
        out.print('\r');
        for (int i = 0; i < lineLength; i++) {
            out.print(' ');
        }
        if (displayFrames) {
            out.printf("\rCompleted - %d of %d - %s%n", imageNumber, numberOfFrames, file.getName());
        } else {
            out.printf("\rCompleted - %s", file.getName());
        }
        complete = true;
        indiElement.removeINDIElementListener(this);
    }
}
