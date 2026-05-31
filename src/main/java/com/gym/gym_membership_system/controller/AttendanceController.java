package com.gym.gym_membership_system.controller;

import com.gym.gym_membership_system.dto.AccessCodeResponse;
import com.gym.gym_membership_system.dto.AttendanceRecordResponse;
import com.gym.gym_membership_system.dto.CheckInRequest;
import com.gym.gym_membership_system.dto.CheckInResponse;
import com.gym.gym_membership_system.service.AccessCodeService;
import com.gym.gym_membership_system.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AccessCodeService accessCodeService;

    public AttendanceController(AttendanceService attendanceService,
                                AccessCodeService accessCodeService) {
        this.attendanceService = attendanceService;
        this.accessCodeService = accessCodeService;
    }

    @GetMapping("/member/visits")
    public ResponseEntity<List<AttendanceRecordResponse>> getMyVisits(@RequestParam String email) {
        return ResponseEntity.ok(attendanceService.getMemberVisits(email));
    }

    @PostMapping("/member/check-in")
    public ResponseEntity<CheckInResponse> checkIn(@RequestParam String email,
                                                   @RequestBody CheckInRequest request) {
        return ResponseEntity.ok(attendanceService.checkIn(email, request.getCode()));
    }

    @GetMapping("/admin/access-code/current")
    public ResponseEntity<AccessCodeResponse> getCurrentAccessCode() {
        return ResponseEntity.ok(
                new AccessCodeResponse(
                        accessCodeService.getCurrentCode(),
                        accessCodeService.getRemainingSeconds()
                )
        );
    }

    @GetMapping("/admin/attendance/today")
    public ResponseEntity<List<AttendanceRecordResponse>> getTodayAttendance() {
        return ResponseEntity.ok(attendanceService.getTodayAttendance());
    }
}
