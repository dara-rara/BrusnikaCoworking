package com.example.BrusnikaCoworking.adapter.repository;

import com.example.BrusnikaCoworking.domain.coworking.CoworkingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CoworkingRepository extends JpaRepository<CoworkingEntity,Long> {

    CoworkingEntity findByNumber(Integer number);

    @Query(value = "SELECT c.number\n" +
            "FROM Coworking c\n" +
            "WHERE c.id_table NOT IN (\n" +
            "    SELECT r.id_table\n" +
            "    FROM Reservals r\n" +
            "    WHERE r.date = :date\n" +
            "    AND r.state_reserval = 'TRUE'\n" +
            "    AND ((:time1 BETWEEN r.time_start AND r.time_end\n" +
            "    OR :time2 BETWEEN r.time_start AND r.time_end)\n" +
            "    OR (:time1 < r.time_start AND :time2 > r.time_end))\n" +
            ") ORDER BY c.number ASC", nativeQuery = true)
    List<Integer> findByNotReservalTable(@Param("date") Date date, @Param("time1") Date time1, @Param("time2") Date time2);
}

