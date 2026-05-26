package com.gym.gym_membership_system.controller.frontend;

import com.gym.gym_membership_system.dto.frontend.AccessCodeDto;
import com.gym.gym_membership_system.dto.frontend.ChangePlanRequest;
import com.gym.gym_membership_system.dto.frontend.CheckInRequest;
import com.gym.gym_membership_system.dto.frontend.CheckInResponse;
import com.gym.gym_membership_system.dto.frontend.CurrentMemberDto;
import com.gym.gym_membership_system.dto.frontend.PlanDto;
import com.gym.gym_membership_system.dto.frontend.TrainingSessionDto;
import com.gym.gym_membership_system.dto.frontend.VisitRecordDto;
import com.gym.gym_membership_system.service.FrontendDemoDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FrontendApiController {

    private final FrontendDemoDataService frontendDemoDataService;

    public FrontendApiController(FrontendDemoDataService frontendDemoDataService) {
        this.frontendDemoDataService = frontendDemoDataService;
    }

    @GetMapping("/api/member/me")
    public CurrentMemberDto getCurrentMember() {
        return frontendDemoDataService.getCurrentMember();
    }

    @GetMapping("/api/member/me/visits")
    public List<VisitRecordDto> getCurrentMemberVisits() {
        return frontendDemoDataService.getVisits();
    }

    @PostMapping("/api/member/me/change-plan")
    public ResponseEntity<?> changePlan(@RequestBody ChangePlanRequest request) {
        try {
            return ResponseEntity.ok(frontendDemoDataService.changePlan(request.planName()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @GetMapping("/api/plans")
    public List<PlanDto> getPlans() {
        return frontendDemoDataService.getPlans();
    }

    @GetMapping("/api/sessions")
    public List<TrainingSessionDto> getSessions() {
        return frontendDemoDataService.getSessions();
    }

    @PostMapping("/api/sessions/{id}/register")
    public ResponseEntity<?> registerForSession(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(frontendDemoDataService.registerForSession(id));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @GetMapping("/api/admin/access-code/current")
    public AccessCodeDto getCurrentAccessCode() {
        return frontendDemoDataService.getCurrentAccessCode();
    }

    @PostMapping("/api/check-in")
    public CheckInResponse checkIn(@RequestBody CheckInRequest request) {
        return frontendDemoDataService.checkIn(request.code());
    }
}
