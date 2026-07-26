package org.booklore.repository;

import org.booklore.model.entity.LibraryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibraryRepository extends JpaRepository<LibraryEntity, Long>, JpaSpecificationExecutor<LibraryEntity> {

    List<LibraryEntity> findByIdIn(List<Long> ids);

    @Modifying(flushAutomatically = true)
    @Query(value = "DELETE FROM user_library_mapping WHERE library_id = :libraryId", nativeQuery = true)
    void deleteUserLibraryMappingsByLibraryId(long libraryId);
}
