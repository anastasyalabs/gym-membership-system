package com.gym.gym_membership_system.service;

import com.gym.gym_membership_system.domain.AttendanceRecord;
import com.gym.gym_membership_system.domain.Member;
import com.gym.gym_membership_system.dto.AttendanceRecordResponse;
import com.gym.gym_membership_system.dto.CheckInResponse;
import com.gym.gym_membership_system.repository.AttendanceRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final MemberService memberService;
    private final SubscriptionService subscriptionService;
    private final AccessCodeService accessCodeService;

    public AttendanceService(AttendanceRecordRepository attendanceRecordRepository,
                             MemberService memberService,
                             SubscriptionService subscriptionService,
                             AccessCodeService accessCodeService) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.memberService = memberService;
        this.subscriptionService = subscriptionService;
        this.accessCodeService = accessCodeService;
    }

    public List<AttendanceRecordResponse> getMemberVisits(String email) {
        Member member = memberService.getMemberEntityByEmail(email);

        return attendanceRecordRepository.findByMemberOrderByVisitDateDescVisitTimeDesc(member)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceRecordResponse> getTodayAttendance() {
        return attendanceRecordRepository.findByVisitDateOrderByVisitTimeDesc(LocalDate.now())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CheckInResponse checkIn(String email, String code) {
        Member member = memberService.getMemberEntityByEmail(email);

        // Member must have an active subscription before entering the gym.
        subscriptionService.checkMemberHasActiveSubscription(member);

        if (!accessCodeService.isValidCode(code)) {
            return new CheckInResponse(
                    false,
                    "Invalid access code. Please ask gym staff for the current code.",
                    getMemberVisits(email)
            );
        }

        LocalDate today = LocalDate.now();

        if (attendanceRecordRepository.existsByMemberAndVisitDate(member, today)) {
            return new CheckInResponse(
                    true,
                    "You have already checked in today.",
                    getMemberVisits(email)
            );
        }

        AttendanceRecord record = new AttendanceRecord(
                member,
                today,
                LocalTime.now().withSecond(0).withNano(0)
        );

        attendanceRecordRepository.save(record);

        return new CheckInResponse(
                true,
                "Check-in successful. Welcome to the gym!",
                getMemberVisits(email)
        );
    }

    private AttendanceRecordResponse toResponse(AttendanceRecord record) {
        String memberName = record.getMember().getName() + " " + record.getMember().getSurname();

        return new AttendanceRecordResponse(
                record.getId(),
                record.getMember().getId(),
                memberName,
                record.getVisitDate().toString(),
                record.getVisitTime().toString()
        );
    }
}
