package com.nubianlanguages.pronounciationservice.service;



import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

@Service
public class AudioConversionService {

    public File normalizeToMono16kWav(File inputFile) throws Exception {


                AudioInputStream originalAis = AudioSystem.getAudioInputStream(inputFile);
            AudioFormat sourceFormat = originalAis.getFormat();


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

    }
}