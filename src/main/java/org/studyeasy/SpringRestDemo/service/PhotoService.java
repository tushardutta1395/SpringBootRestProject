package org.studyeasy.SpringRestDemo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.studyeasy.SpringRestDemo.model.Photo;
import org.studyeasy.SpringRestDemo.repository.PhotoRepository;

@Service
public class PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    public void save(final Photo photo) {
        photoRepository.save(photo);
    }

    public Optional<Photo> findById(final Long id) {
        return photoRepository.findById(id);
    }

    public List<Photo> findByAlbumId(final Long id) {
        return photoRepository.findByAlbumId(id);
    }

    public void delete(final Photo photo) {
        photoRepository.delete(photo);
    }
}
