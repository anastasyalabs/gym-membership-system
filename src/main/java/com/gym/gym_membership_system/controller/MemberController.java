package com.gym.gym_membership_system.controller;

import com.gym.gym_membership_system.dto.MemberResponse;
import com.gym.gym_membership_system.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // ─── Member — view own profile ────────────────────────────────────────────
    @GetMapping("/member/profile")
    public ResponseEntity<MemberResponse> getProfile(
            @RequestParam String email) {
        return ResponseEntity.ok(memberService.getProfile(email));
    }

    // ─── Member — update own profile ──────────────────────────────────────────
    @PutMapping("/member/profile")
    public ResponseEntity<MemberResponse> updateProfile(
            @RequestParam String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) LocalDate dateOfBirth) {
        return ResponseEntity.ok(
                memberService.updateProfile(email, name, surname, phone, dateOfBirth));
    }

    // ─── Admin — view all members ─────────────────────────────────────────────
    @GetMapping("/admin/members")
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    // ─── Admin — deactivate a member ─────────────────────────────────────────
    @PutMapping("/admin/members/{id}/deactivate")
    public ResponseEntity<Void> deactivateMember(@PathVariable Long id) {
        memberService.deactivateMember(id);
        return ResponseEntity.noContent().build();
    }
}