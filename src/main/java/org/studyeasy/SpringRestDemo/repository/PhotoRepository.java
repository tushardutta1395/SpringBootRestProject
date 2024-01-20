package org.studyeasy.SpringRestDemo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.studyeasy.SpringRestDemo.model.Photo;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    List<Photo> findByAlbumId(final Long id);
}
