package com.gym.gym_membership_system.repository;

import com.gym.gym_membership_system.domain.AttendanceRecord;
import com.gym.gym_membership_system.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByMemberOrderByVisitDateDescVisitTimeDesc(Member member);

    List<AttendanceRecord> findByVisitDateOrderByVisitTimeDesc(LocalDate visitDate);

    boolean existsByMemberAndVisitDate(Member member, LocalDate visitDate);
}
