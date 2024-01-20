package org.studyeasy.SpringRestDemo.util.AppUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;


public class AppUtil {

    private static final String PATH = "src\\main\\resources\\static\\uploads\\";

    public static String get_photo_upload_path(final String fileName, final String folder_name, final Long album_id) throws IOException {
        final var path = PATH + album_id + "\\" + folder_name;
        Files.createDirectories(Paths.get(path));
        return new File(path).getAbsolutePath() + "\\" + fileName;
    }

    public static BufferedImage getThumbnail(final MultipartFile originalFile, final Integer width) throws IOException {
        final var img = ImageIO.read(originalFile.getInputStream());
        return Scalr.resize(img, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, width, Scalr.OP_ANTIALIAS);
    }

    public static Resource getFileAsResource(final Long album_id, final String folder_name, final String file_name) throws IOException {
        final var location = PATH + album_id + "\\" + folder_name + "\\" + file_name;
        final var file = new File(location);
        if (file.exists()) {
            final var path = Paths.get(file.getAbsolutePath());
            return new UrlResource(path.toUri());
        } else {
            return null;
        }
    }

    public static Boolean delete_photo_from_path(final String fileName, final String folder_name, final Long album_id) {
        try {
            final var file = new File(PATH + album_id + "\\" + folder_name + "\\" + fileName); // file to be deleted
            return file.delete();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
