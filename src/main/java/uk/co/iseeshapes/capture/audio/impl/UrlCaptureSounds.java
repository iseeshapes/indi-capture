package uk.co.iseeshapes.capture.audio.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.iseeshapes.capture.audio.CaptureSounds;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UrlCaptureSounds implements CaptureSounds {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(UrlCaptureSounds.class);

    public UrlCaptureSounds() {
        CodeSource codeSource = UrlCaptureSounds.class.getProtectionDomain().getCodeSource();
        try (ZipInputStream zin = new ZipInputStream(codeSource.getLocation().openStream())) {
            ZipEntry entry;

            while ((entry = zin.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.endsWith(".wav")) {
                    log.info("Found .wav : {}", name);
                }
            }
        } catch (IOException e) {
            log.info("Failed to find .wav files");
        }
    }

    private void playSound (URL url) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(url));
            clip.start();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            log.error("Cannot play sound {}", url.getPath());
        }
    }

    @Override
    public void playCapture () {
        URL url = UrlCaptureSounds.class.getResource("/wav/38700__elanhickler__archi-sonar-01.wav");
        playSound(url);
    }

    @Override
    public void playEndSequence () {
        URL url = UrlCaptureSounds.class.getResource("/wav/107341__thompsonman__beeps.wav");
        playSound(url);
    }
}
