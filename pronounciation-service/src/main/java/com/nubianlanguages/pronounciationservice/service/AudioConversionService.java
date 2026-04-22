package com.nubianlanguages.pronounciationservice.service;



import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

@Service
public class AudioConversionService {

    public File normalizeToMono16kWav(File inputFile) throws Exception {
        System.out.println("Before TRY INPUT FILE: name " + inputFile.getName());
       // try (
                AudioInputStream originalAis = AudioSystem.getAudioInputStream(inputFile);//) {
            AudioFormat sourceFormat = originalAis.getFormat();
           // AudioFormat sourceFormat = sourceAis.getFormat();
            System.out.println("INPUT FILE: name " + inputFile.getName());
            System.out.println("INPUT FILE: " + inputFile.getAbsolutePath());
            System.out.println("Source encoding: " + sourceFormat.getEncoding());
            System.out.println("Source sample rate: " + sourceFormat.getSampleRate());
            System.out.println("Source channels: " + sourceFormat.getChannels());
            System.out.println("Source sample size bits: " + sourceFormat.getSampleSizeInBits());
            System.out.println("Source frame size: " + sourceFormat.getFrameSize());
            System.out.println("Source big endian: " + sourceFormat.isBigEndian());
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    16000,
                    16,
                    1,
                    2,
                    16000,
                    false
            );

            AudioInputStream convertedAis = AudioSystem.getAudioInputStream(targetFormat, originalAis);


            File normalizedFile = File.createTempFile("normalized-", ".wav");
            AudioSystem.write(convertedAis, AudioFileFormat.Type.WAVE, normalizedFile);

            convertedAis.close();

            return normalizedFile;
//        } catch (UnsupportedAudioFileException e) {
//            throw new IllegalArgumentException("Unsupported audio format. Please upload a WAV file for now.", e);
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to normalize audio.", e);
//        }
    }
}