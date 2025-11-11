package com.aimovie.repository;

import com.aimovie.entity.User;
import com.aimovie.entity.WatchlistCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistCollectionRepository extends JpaRepository<WatchlistCollection, Long> {

    List<WatchlistCollection> findByUserAndIsDefaultFalse(User user);

    List<WatchlistCollection> findByUser(User user);

    Optional<WatchlistCollection> findByUserAndIsDefaultTrue(User user);

    Optional<WatchlistCollection> findByUserAndName(User user, String name);

    @Query("SELECT wc FROM WatchlistCollection wc WHERE wc.user.id = :userId AND wc.name = :name")
    Optional<WatchlistCollection> findByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

    @Query("SELECT wc FROM WatchlistCollection wc WHERE wc.user.id = :userId AND wc.isDefault = true")
    Optional<WatchlistCollection> findDefaultByUserId(@Param("userId") Long userId);

    @Query("SELECT wc FROM WatchlistCollection wc WHERE wc.user.id = :userId ORDER BY wc.isDefault DESC, wc.updatedAt DESC")
    List<WatchlistCollection> findByUserIdOrderByDefaultAndUpdatedAt(@Param("userId") Long userId);

    @Query("SELECT wc FROM WatchlistCollection wc WHERE wc.isPublic = true ORDER BY wc.updatedAt DESC")
    List<WatchlistCollection> findPublicWatchlists();

    @Query("SELECT wc FROM WatchlistCollection wc WHERE wc.user.id = :userId AND wc.isPublic = true")
    List<WatchlistCollection> findPublicWatchlistsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(wc) FROM WatchlistCollection wc WHERE wc.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT wc FROM WatchlistCollection wc WHERE wc.user.id = :userId AND wc.name LIKE %:name%")
    List<WatchlistCollection> findByUserIdAndNameContaining(@Param("userId") Long userId, @Param("name") String name);

    boolean existsByUserAndName(User user, String name);

    boolean existsByUserIdAndName(Long userId, String name);

    @Query("SELECT wc FROM WatchlistCollection wc WHERE wc.user.id = :userId AND wc.id = :collectionId")
    Optional<WatchlistCollection> findByUserIdAndId(@Param("userId") Long userId, @Param("collectionId") Long collectionId);

    void deleteByUser(User user);
}
