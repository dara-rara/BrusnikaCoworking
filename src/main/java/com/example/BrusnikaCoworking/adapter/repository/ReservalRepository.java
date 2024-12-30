package com.example.BrusnikaCoworking.adapter.repository;

import com.example.BrusnikaCoworking.domain.reserval.ReservalEntity;
import com.example.BrusnikaCoworking.domain.reserval.State;
import com.example.BrusnikaCoworking.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservalRepository extends JpaRepository<ReservalEntity,Long> {
    List<ReservalEntity> findByUserAndStateReservalOrderByDateDesc(UserEntity user, State stateReserval);
}

