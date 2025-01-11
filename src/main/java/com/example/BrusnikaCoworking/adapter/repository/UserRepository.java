package com.example.BrusnikaCoworking.adapter.repository;

import com.example.BrusnikaCoworking.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByUsername(String username);
    Boolean existsByUsername(String email);
    List<UserEntity> findByCountBlockGreaterThan(Integer countBlock);

    @Query(value = "SELECT * FROM Users u " +
            "WHERE u.username LIKE :prefix% " +
            "AND u.role <> 'ADMIN' " +
            "AND u.id_user <> :id",
            nativeQuery = true)
    List<UserEntity> findByEmailStartingWith(@Param("prefix") String prefix,
                                             @Param("id") Long id);

}

