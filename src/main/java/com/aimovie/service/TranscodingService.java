package com.aimovie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TranscodingService {

    @Value("${app.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public List<String> transcodeToRenditions(String inputFilename, String outputBasename) throws IOException, InterruptedException {
        Path inputPath = Paths.get(uploadDir).resolve(inputFilename);
        if (!Files.exists(inputPath)) {
            throw new IOException("Input video not found: " + inputFilename);
        }

        // Define target renditions
        String[][] renditions = new String[][]{
                {"360p", "640x360", "800k", "96k"},
                {"480p", "854x480", "1200k", "128k"},
                {"720p", "1280x720", "2800k", "128k"},
                {"1080p", "1920x1080", "5000k", "192k"},
                {"1440p", "2560x1440", "8000k", "256k"}
        };

        List<String> outputs = new ArrayList<>();
        for (String[] r : renditions) {
            String label = r[0];
            String scale = r[1];
            String vBitrate = r[2];
            String aBitrate = r[3];

            String outName = outputBasename + "_" + label + ".mp4";
            Path outPath = Paths.get(uploadDir).resolve(outName);

            List<String> cmd = List.of(
                    ffmpegPath,
                    "-y",
                    "-i", inputPath.toString(),
                    "-vf", "scale=w=" + scale.split("x")[0] + ":h=" + scale.split("x")[1] + ":force_original_aspect_ratio=decrease",
                    "-c:v", "libx264",
                    "-preset", "veryfast",
                    "-b:v", vBitrate,
                    "-maxrate", vBitrate,
                    "-bufsize", String.valueOf((int)(Integer.parseInt(vBitrate.replace("k","")) * 2)) + "k",
                    "-c:a", "aac",
                    "-b:a", aBitrate,
                    outPath.toString()
            );

            log.info("Transcoding {} -> {} ({} {})", inputFilename, outName, scale, vBitrate);
            Process process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            int exit = process.waitFor();
            if (exit != 0) {
                log.warn("FFmpeg exited with code {} for rendition {}", exit, label);
            } else {
                outputs.add(outName);
            }
        }

        return outputs;
    }
}


