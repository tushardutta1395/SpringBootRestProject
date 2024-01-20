package org.studyeasy.SpringRestDemo.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.studyeasy.SpringRestDemo.model.Album;
import org.studyeasy.SpringRestDemo.model.Photo;
import org.studyeasy.SpringRestDemo.payload.album.AlbumPayloadDTO;
import org.studyeasy.SpringRestDemo.payload.album.AlbumViewDTO;
import org.studyeasy.SpringRestDemo.payload.album.PhotoDTO;
import org.studyeasy.SpringRestDemo.payload.album.PhotoPayloadDTO;
import org.studyeasy.SpringRestDemo.payload.album.PhotoViewDTO;
import org.studyeasy.SpringRestDemo.service.AccountService;
import org.studyeasy.SpringRestDemo.service.AlbumService;
import org.studyeasy.SpringRestDemo.service.PhotoService;
import org.studyeasy.SpringRestDemo.util.AppUtils.AppUtil;
import org.studyeasy.SpringRestDemo.util.constants.AlbumError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Album Controller", description = "Controller for album and photo management")
@Slf4j
public class AlbumController {

    private static final String PHOTOS_FOLDER_NAME = "photos";
    private static final String THUMBNAIL_FOLDER_NAME = "thumbnails";
    private static final Integer THUMBNAIL_WIDTH = 300;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AlbumService albumService;

    @Autowired
    private PhotoService photoService;

    @PostMapping(value = "/albums/add", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please add valid name and description")
    @ApiResponse(responseCode = "201", description = "Album added")
    @Operation(summary = "Add an Album")
    @SecurityRequirement(name = "studyeasy-demo-api")
    public ResponseEntity<AlbumViewDTO> addAlbum(@Valid @RequestBody final AlbumPayloadDTO albumPayloadDTO,
            final Authentication authentication) {
        try {
            final var album = new Album();
            album.setName(albumPayloadDTO.getName());
            album.setDescription(albumPayloadDTO.getDescription());
            final var email = authentication.getName();
            final var optionalAccount = accountService.findByEmail(email);
            if (optionalAccount.isPresent()) {
                final var account = optionalAccount.get();
                album.setAccount(account);
                final var albumNew = albumService.save(album);
                final var albumViewDTO = new AlbumViewDTO(albumNew.getId(), albumNew.getName(), albumNew.getDescription(),
                        null);
                return ResponseEntity.ok(albumViewDTO);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        } catch (final Exception e) {
            log.debug(AlbumError.ADD_ALBUM_ERROR + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping(value = "/albums", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "List of albums")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @Operation(summary = "List album api")
    @SecurityRequirement(name = "studyeasy-demo-api")
    public List<AlbumViewDTO> albums(final Authentication authentication) {
        final var email = authentication.getName();
        final var optionalAccount = accountService.findByEmail(email);
        if (optionalAccount.isPresent()) {
            final var account = optionalAccount.get();
            final var albums = new ArrayList<AlbumViewDTO>();
            for (final var album : albumService.findByAccountId(account.getId())) {
                final var photos = new ArrayList<PhotoDTO>();
                for (final var photo : photoService.findByAlbumId(album.getId())) {
                    final var link = "/albums/" + album.getId() + "/photos/" + photo.getId() + "/download-photo";
                    photos.add(new PhotoDTO(photo.getId(), photo.getName(), photo.getDescription(), photo.getFileName(),
                            link));
                }
                albums.add(new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(), photos));
            }
            return albums;
        } else {
            return null;
        }
    }

    @GetMapping(value = "/albums/{album_id}", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "List of albums")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token Error")
    @Operation(summary = "List album by album ID")
    @SecurityRequirement(name = "studyeasy-demo-api")
    public ResponseEntity<AlbumViewDTO> albums_by_id(@PathVariable final Long album_id,
            final Authentication authentication) {
        final var email = authentication.getName();
        final var optionalAccount = accountService.findByEmail(email);
        if (optionalAccount.isPresent()) {
            final var account = optionalAccount.get();
            final var optionalAlbum = albumService.findById(album_id);
            Album album;
            if (optionalAlbum.isPresent()) {
                album = optionalAlbum.get();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            if (!account.getId().equals(album.getAccount().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            final var photos = new ArrayList<PhotoDTO>();
            for (final var photo : photoService.findByAlbumId(album.getId())) {
                final var link = "/albums/" + album.getId() + "/photos/" + photo.getId() + "/download-photo";
                photos.add(new PhotoDTO(photo.getId(), photo.getName(), photo.getDescription(), photo.getFileName(), link));
            }

            final var albumViewDTO = new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(), photos);

            return ResponseEntity.ok(albumViewDTO);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping(value = "/albums/{album_id}/update", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please add valid name and description")
    @ApiResponse(responseCode = "204", description = "Album updated")
    @Operation(summary = "Update an Album")
    @SecurityRequirement(name = "studyeasy-demo-api")
    public ResponseEntity<AlbumViewDTO> update_Album(@Valid @RequestBody final AlbumPayloadDTO albumPayloadDTO,
            @PathVariable final Long album_id, final Authentication authentication) {
        try {
            final var email = authentication.getName();
            final var optionalAccount = accountService.findByEmail(email);
            if (optionalAccount.isPresent()) {
                final var account = optionalAccount.get();

                final var optionalAlbum = albumService.findById(album_id);
                Album album;
                if (optionalAlbum.isPresent()) {
                    album = optionalAlbum.get();
                    if (!account.getId().equals(album.getAccount().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }

                album.setName(albumPayloadDTO.getName());
                album.setDescription(albumPayloadDTO.getDescription());
                album = albumService.save(album);
                final var photos = new ArrayList<PhotoDTO>();
                for (final var photo : photoService.findByAlbumId(album.getId())) {
                    final var link = "/albums/" + album.getId() + "/photos/" + photo.getId() + "/download-photo";
                    photos.add(new PhotoDTO(photo.getId(), photo.getName(), photo.getDescription(), photo.getFileName(),
                            link));
                }
                final var albumViewDTO = new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(), photos);
                return ResponseEntity.ok(albumViewDTO);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        } catch (final Exception e) {
            log.debug(AlbumError.ADD_ALBUM_ERROR + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping(value = "/albums/{album_id}/upload-photos", consumes = { "multipart/form-data" })
    @Operation(summary = "Upload photo into album")
    @ApiResponse(responseCode = "400", description = "Please check the payload or token")
    @SecurityRequirement(name = "studyeasy-demo-api")
    public ResponseEntity<ArrayList<HashMap<String, ArrayList<?>>>> photos(
            @RequestPart() final MultipartFile[] files,
            @PathVariable final Long album_id, final Authentication authentication) {
        final var email = authentication.getName();
        final var optionalAccount = accountService.findByEmail(email);
        if (optionalAccount.isPresent()) {
            final var account = optionalAccount.get();
            final var optionalAlbum = albumService.findById(album_id);
            Album album;
            if (optionalAlbum.isPresent()) {
                album = optionalAlbum.get();
                if (!account.getId().equals(album.getAccount().getId())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            final var fileNamesWithSuccess = new ArrayList<PhotoViewDTO>();
            final var fileNamesWithError = new ArrayList<String>();
            Arrays.asList(files).forEach(file -> {
                final var contentType = file.getContentType();
                if ((contentType != null && contentType.equals("image/png")) || (contentType != null && contentType.equals("image/jpg"))
                        || (contentType != null && contentType.equals("image/jpeg"))) {
                    final var length = 10;
                    final var useLetters = true;
                    final var useNumbers = true;
                    try {
                        final var fileName = file.getOriginalFilename();
                        final var generatedString = RandomStringUtils.random(length, useLetters, useNumbers);
                        final var final_photo_name = generatedString + fileName;
                        final var absolute_fileLocation = AppUtil.get_photo_upload_path(final_photo_name,
                                PHOTOS_FOLDER_NAME, album_id);
                        final var path = Paths.get(absolute_fileLocation);
                        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                        final var photo = new Photo();
                        photo.setName(fileName);
                        photo.setFileName(final_photo_name);
                        photo.setOriginalFileName(fileName);
                        photo.setAlbum(album);
                        photoService.save(photo);

                        final var photoViewDTO = new PhotoViewDTO(photo.getId(), photo.getName(), photo.getDescription());
                        fileNamesWithSuccess.add(photoViewDTO);

                        final var thumbImg = AppUtil.getThumbnail(file, THUMBNAIL_WIDTH);
                        final var thumbnail_location = new File(
                                AppUtil.get_photo_upload_path(final_photo_name, THUMBNAIL_FOLDER_NAME, album_id));
                        ImageIO.write(thumbImg, file.getContentType().split("/")[1], thumbnail_location);
                    } catch (final Exception e) {
                        log.debug(AlbumError.PHOTO_UPLOAD_ERROR + ": " + e.getMessage());
                        fileNamesWithError.add(file.getOriginalFilename());
                    }
                } else {
                    fileNamesWithError.add(file.getOriginalFilename());
                }
            });
            final var result = new HashMap<String, ArrayList<?>>();
            result.put("SUCCESS", fileNamesWithSuccess);
            result.put("ERRORS", fileNamesWithError);
            final var response = new ArrayList<HashMap<String, ArrayList<?>>>();
            response.add(result);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/albums/{album_id}/photos/{photo_id}/download-photo")
    @SecurityRequirement(name = "studyeasy-demo-api")
    public ResponseEntity<?> downloadPhoto(@PathVariable("album_id") final Long album_id,
            @PathVariable("photo_id") final Long photo_id, final Authentication authentication) {
        return downloadFile(album_id, photo_id, authentication);
    }

    @GetMapping("/albums/{album_id}/photos/{photo_id}/download-thumbnail")
    @SecurityRequirement(name = "studyeasy-demo-api")
    public ResponseEntity<?> downloadThumbnail(@PathVariable("album_id") final Long album_id,
            @PathVariable("photo_id") final Long photo_id, final Authentication authentication) {
        return downloadFile(album_id, photo_id, authentication);
    }

    public ResponseEntity<?> downloadFile(final Long album_id, final Long photo_id,
                                          final Authentication authentication) {
        final var email = authentication.getName();
        final var optionalAccount = accountService.findByEmail(email);
        if (optionalAccount.isPresent()) {
            final var account = optionalAccount.get();

            final var optionalAlbum = albumService.findById(album_id);
            Album album;
            if (optionalAlbum.isPresent()) {
                album = optionalAlbum.get();
                if (!account.getId().equals(album.getAccount().getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            final var optionalPhoto = photoService.findById(photo_id);
            if (optionalPhoto.isPresent()) {
                final var photo = optionalPhoto.get();
                if (!photo.getAlbum().getId().equals(album_id)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
                Resource resource;
                try {
                    resource = AppUtil.getFileAsResource(album_id, PHOTOS_FOLDER_NAME, photo.getFileName());
                } catch (final IOException e) {
                    return ResponseEntity.internalServerError().build();
                }
                if (resource == null) {
                    return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
                }
                final var contentType = "application/octet-stream";
                final var headerValue = "attachment; filename=\"" + photo.getOriginalFileName() + "\"";

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping(value = "/albums/{album_id}/photos/{photo_id}/update")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please add valid name and description")
    @ApiResponse(responseCode = "204", description = "Photo updated")
    @Operation(summary = "Update a photo")
    @SecurityRequirement(name = "studyeasy-demo-api")
    public ResponseEntity<PhotoViewDTO> update_photo(@Valid @RequestBody final PhotoPayloadDTO photoPayloadDTO,
            @PathVariable final Long album_id, @PathVariable final Long photo_id, final Authentication authentication) {
        try {
            final var email = authentication.getName();
            final var optionalAccount = accountService.findByEmail(email);
            if (optionalAccount.isPresent()) {
                final var account = optionalAccount.get();

                final var optionalAlbum = albumService.findById(album_id);
                Album album;
                if (optionalAlbum.isPresent()) {
                    album = optionalAlbum.get();
                    if (!account.getId().equals(album.getAccount().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
                final var optionalPhoto = photoService.findById(photo_id);
                if (optionalPhoto.isPresent()) {
                    final var photo = optionalPhoto.get();
                    if (!photo.getAlbum().getId().equals(album_id)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                    }
                    photo.setName(photoPayloadDTO.getName());
                    photo.setDescription(photoPayloadDTO.getDescription());
                    photoService.save(photo);
                    final var photoViewDTO = new PhotoViewDTO(photo.getId(), photoPayloadDTO.getName(),
                            photoPayloadDTO.getDescription());
                    return ResponseEntity.ok(photoViewDTO);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        } catch (final Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping(value = "/albums/{album_id}/photos/{photo_id}/delete")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "202", description = "Photo deleted")
    @Operation(summary = "Delete a photo")
    @SecurityRequirement(name = "studyeasy-demo-api")
    public ResponseEntity<String> delete_photo(@PathVariable final Long album_id, @PathVariable final Long photo_id, final Authentication authentication) {
        try {
            final var email = authentication.getName();
            final var optionalAccount = accountService.findByEmail(email);
            if (optionalAccount.isPresent()) {
                final var account = optionalAccount.get();

                final var optionalAlbum = albumService.findById(album_id);
                Album album;
                if (optionalAlbum.isPresent()) {
                    album = optionalAlbum.get();
                    if (!account.getId().equals(album.getAccount().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }

                final var optionalPhoto = photoService.findById(photo_id);
                if (optionalPhoto.isPresent()) {
                    final var photo = optionalPhoto.get();
                    if (!photo.getAlbum().getId().equals(album_id)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                    }

                    final var delete_photo = AppUtil.delete_photo_from_path(photo.getFileName(), PHOTOS_FOLDER_NAME, album_id);
                    final var delete_thumbnail = AppUtil.delete_photo_from_path(photo.getFileName(), THUMBNAIL_FOLDER_NAME, album_id);
                    if (delete_photo && delete_thumbnail) {
                        photoService.delete(photo);
                        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        } catch (final Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping(value = "/albums/{album_id}/delete")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "202", description = "Album deleted")
    @Operation(summary = "Delete an album")
    @SecurityRequirement(name = "studyeasy-demo-api")
    public ResponseEntity<String> delete_album(@PathVariable final Long album_id, final Authentication authentication) {
        try {
            final var email = authentication.getName();
            final var optionalAccount = accountService.findByEmail(email);
            if (optionalAccount.isPresent()) {
                final var account = optionalAccount.get();

                final var optionalAlbum = albumService.findById(album_id);
                Album album;
                if (optionalAlbum.isPresent()) {
                    album = optionalAlbum.get();
                    if (!account.getId().equals(album.getAccount().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }

                for (final var photo : photoService.findByAlbumId(album.getId())) {
                    AppUtil.delete_photo_from_path(photo.getFileName(), PHOTOS_FOLDER_NAME, album_id);
                    AppUtil.delete_photo_from_path(photo.getFileName(), THUMBNAIL_FOLDER_NAME, album_id);
                    photoService.delete(photo);
                }
                albumService.deleteAlbum(album);
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        } catch (final Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
