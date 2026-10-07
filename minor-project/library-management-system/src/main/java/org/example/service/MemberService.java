package org.example.service;

import org.example.dao.MemberDAO;
import org.example.model.Member;

import java.util.List;
import java.util.regex.Pattern;

public class MemberService {

    private final MemberDAO memberDAO;

    public MemberService() {
        this.memberDAO = new MemberDAO();
    }

    public boolean addMember(Member member) {

        if (!isValidMember(member)) {
            return false;
        }

        return memberDAO.addMember(member);
    }

    public boolean updateMember(Member member) {

        if (!isValidMember(member)) {
            return false;
        }

        return memberDAO.updateMember(member);
    }

    public boolean deleteMember(int memberId) {

        if (memberId <= 0) {
            return false;
        }

        return memberDAO.deleteMember(memberId);
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

    private boolean isValidMember(Member member) {

        if (member == null) {
            return false;
        }

        if (member.getName() == null ||
                member.getName().isBlank()) {
            return false;
        }

        if (member.getEmail() == null ||
                member.getEmail().isBlank()) {
            return false;
        }

        // Basic email validation
        if (!Pattern.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
                member.getEmail())) {
            return false;
        }

        if (member.getPhone() != null &&
                !member.getPhone().isBlank() &&
                !member.getPhone().matches("\\d{10}")) {
            return false;
        }

        return true;
    }
}