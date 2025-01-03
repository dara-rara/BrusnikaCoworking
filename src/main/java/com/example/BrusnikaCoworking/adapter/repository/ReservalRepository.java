package com.example.BrusnikaCoworking.adapter.repository;

import com.example.BrusnikaCoworking.domain.reserval.ReservalEntity;
import com.example.BrusnikaCoworking.domain.reserval.State;
import com.example.BrusnikaCoworking.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReservalRepository extends JpaRepository<ReservalEntity,Long> {
    List<ReservalEntity> findByUserAndStateReservalOrderByDateDesc(UserEntity user, State stateReserval);

    @Query(value = "SELECT r.id_reserval FROM Reservals r\n" +
            "WHERE r.date = :date\n" +
            "AND r.id_user = :user\n" +
            "AND r.state_reserval = 'TRUE'\n" +
            "AND ((:time1 BETWEEN r.time_start AND r.time_end\n" +
            "OR :time2 BETWEEN r.time_start AND r.time_end)\n" +
            "OR (:time1 < r.time_start AND :time2 > r.time_end))\n",
            nativeQuery = true)
    List<Long> findActiveReservalsInTimeRangeForUser(@Param("date") Date date,
                                                     @Param("time1") Date time1,
                                                     @Param("time2") Date time2,
                                                     @Param("user") Long user);

}

