package org.studyeasy.SpringRestDemo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.studyeasy.SpringRestDemo.model.Album;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findByAccountId(final Long id);
}
