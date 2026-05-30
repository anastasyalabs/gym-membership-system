package com.gym.gym_membership_system.service;

import com.gym.gym_membership_system.domain.Member;
import com.gym.gym_membership_system.domain.UserAccount;
import com.gym.gym_membership_system.domain.UserStatus;
import com.gym.gym_membership_system.dto.MemberResponse;
import com.gym.gym_membership_system.exception.ResourceNotFoundException;
import com.gym.gym_membership_system.repository.MemberRepository;
import com.gym.gym_membership_system.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserAccountService userAccountService;

    public MemberService(MemberRepository memberRepository,
                         UserAccountRepository userAccountRepository,
                         UserAccountService userAccountService) {
        this.memberRepository = memberRepository;
        this.userAccountRepository = userAccountRepository;
        this.userAccountService = userAccountService;
    }

    // ─── Get profile of currently logged-in member ───────────────────────────

    public MemberResponse getProfile(String email) {
        UserAccount account = userAccountService.getByEmail(email);
        Member member = memberRepository.findByUserAccount(account)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Member profile not found for email: " + email));
        return toResponse(member);
    }

    // ─── Update profile of currently logged-in member ────────────────────────

    @Transactional
    public MemberResponse updateProfile(String email, String name,
                                        String surname, String phone,
                                        LocalDate dateOfBirth) {
        UserAccount account = userAccountService.getByEmail(email);
        Member member = memberRepository.findByUserAccount(account)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Member profile not found for email: " + email));

        // Only update fields that were actually provided
        if (name != null)        member.setName(name);
        if (surname != null)     member.setSurname(surname);
        if (phone != null)       member.setPhone(phone);
        if (dateOfBirth != null) member.setDateOfBirth(dateOfBirth);

        memberRepository.save(member);
        return toResponse(member);
    }

    // ─── Admin — get all members ─────────────────────────────────────────────

    public List<MemberResponse> getAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Admin — deactivate a member ─────────────────────────────────────────

    @Transactional
    public void deactivateMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Member not found with id: " + memberId));

        // Set the UserAccount status to INACTIVE
        UserAccount account = member.getUserAccount();
        account.setStatus(UserStatus.INACTIVE);
        userAccountRepository.save(account);
    }

    // ─── Helper — get Member entity by email (used by other services) ────────

    public Member getMemberEntityByEmail(String email) {
        UserAccount account = userAccountService.getByEmail(email);
        return memberRepository.findByUserAccount(account)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Member not found for email: " + email));
    }

    // ─── Helper — convert Member entity to MemberResponse DTO ───────────────

    private MemberResponse toResponse(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getUserAccount().getEmail(),
                member.getName(),
                member.getSurname(),
                member.getPhone(),
                member.getDateOfBirth(),
                member.getUserAccount().getStatus().name()
        );
    }
}