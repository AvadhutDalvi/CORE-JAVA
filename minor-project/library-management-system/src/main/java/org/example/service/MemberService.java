package org.example.service;

import org.example.dao.MemberDAO;
import org.example.model.Member;
import org.example.util.ValidationException;

import java.util.List;
import java.util.regex.Pattern;

public class MemberService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{10}$");

    private final MemberDAO memberDAO;

    public MemberService() {
        this.memberDAO = new MemberDAO();
    }

    public MemberService(MemberDAO memberDAO) {
        this.memberDAO = memberDAO;
    }

    public boolean addMember(Member member) {
        if (member == null) {
            throw new ValidationException("Member details cannot be empty.");
        }

        validateMemberFields(member);

        String email = member.getEmail().trim().toLowerCase();
        member.setEmail(email);

        if (memberDAO.isEmailExists(email, 0)) {
            throw new ValidationException("A member with email '" + email + "' already exists.");
        }

        boolean success = memberDAO.addMember(member);
        if (!success) {
            throw new ValidationException("Failed to add member to database. Please try again.");
        }
        return true;
    }

    public boolean updateMember(Member member) {
        if (member == null) {
            throw new ValidationException("Member details cannot be empty.");
        }

        if (member.getMemberId() <= 0) {
            throw new ValidationException("Invalid Member ID for update.");
        }

        validateMemberFields(member);

        Member existing = memberDAO.getMemberById(member.getMemberId());
        if (existing == null) {
            throw new ValidationException("Member not found (ID: " + member.getMemberId() + ").");
        }

        String email = member.getEmail().trim().toLowerCase();
        member.setEmail(email);

        if (memberDAO.isEmailExists(email, member.getMemberId())) {
            throw new ValidationException("Email '" + email + "' is already registered to another member.");
        }

        boolean success = memberDAO.updateMember(member);
        if (!success) {
            throw new ValidationException("Failed to update member in database. Please try again.");
        }
        return true;
    }

    public boolean deleteMember(int memberId) {
        if (memberId <= 0) {
            throw new ValidationException("Invalid Member ID.");
        }

        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            throw new ValidationException("Member not found (ID: " + memberId + ").");
        }

        if (memberDAO.hasActiveBorrowings(memberId)) {
            throw new ValidationException("Cannot delete member: member currently has borrowed books that must be returned first.");
        }

        if (memberDAO.hasAnyTransactions(memberId)) {
            throw new ValidationException("Cannot delete member: transaction records exist for this member in history.");
        }

        boolean success = memberDAO.deleteMember(memberId);
        if (!success) {
            throw new ValidationException("Failed to delete member. It may be referenced by other records.");
        }
        return true;
    }

    public Member getMemberById(int memberId) {
        if (memberId <= 0) {
            return null;
        }
        return memberDAO.getMemberById(memberId);
    }

    public List<Member> getAllMembers() {
        return memberDAO.getAllMembers();
    }

    public List<Member> searchMembers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllMembers();
        }
        return memberDAO.searchMembers(keyword.trim());
    }

    private void validateMemberFields(Member member) {
        if (member.getName() == null || member.getName().trim().isEmpty()) {
            throw new ValidationException("Member name is required.");
        }

        if (member.getName().trim().length() < 2) {
            throw new ValidationException("Member name must be at least 2 characters long.");
        }

        if (member.getEmail() == null || member.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email address is required.");
        }

        String email = member.getEmail().trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email address format (e.g., user@example.com).");
        }

        if (member.getPhone() != null && !member.getPhone().trim().isEmpty()) {
            String phone = member.getPhone().trim();
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                throw new ValidationException("Invalid phone number. It must be exactly 10 digits.");
            }
            member.setPhone(phone);
        } else {
            member.setPhone(null);
        }

        // Clean trimmed fields
        member.setName(member.getName().trim());
        if (member.getAddress() != null) {
            member.setAddress(member.getAddress().trim());
        }
    }
}