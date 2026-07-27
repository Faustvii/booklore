package org.booklore.repository;

import org.booklore.model.entity.PdfAnnotationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface PdfAnnotationRepository extends JpaRepository<PdfAnnotationEntity, Long> {

    Optional<PdfAnnotationEntity> findByBookIdAndUserId(Long bookId, Long userId);

    void deleteByBookIdAndUserId(Long bookId, Long userId);

    @Modifying
    @Query("DELETE FROM PdfAnnotationEntity p WHERE p.bookId IN :bookIds")
    void deleteByBookIdIn(@Param("bookIds") Collection<Long> bookIds);
}
