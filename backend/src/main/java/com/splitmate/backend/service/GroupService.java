package com.splitmate.backend.service;

import com.splitmate.backend.model.Group;
import com.splitmate.backend.model.GroupMember;
import com.splitmate.backend.repository.GroupMemberRepository;
import com.splitmate.backend.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public Group createGroup(String name, String groupType, String createdBy) {
        Group group = Group.builder()
                .name(name)
                .groupType(normalizeGroupType(groupType))
                .createdBy(createdBy)
                .build();
        group = groupRepository.save(group);

        // Add creator as ADMIN
        groupMemberRepository.save(GroupMember.builder()
                .groupId(group.getId())
                .userId(createdBy)
                .role("ADMIN")
                .build());

        return group;
    }

    public Group updateGroup(String groupId, String name, String photoUrl) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        group.setName(name);
        group.setPhotoUrl(photoUrl);
        return groupRepository.save(group);
    }

    public void deleteGroup(String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        group.setArchived(true);
        groupRepository.save(group);
    }

    public String generateInviteLink(String groupId) {
        // Returns a shareable link for joining the group
        return "https://splitmate.app/join/" + groupId;
    }

    public void joinGroup(String groupId, String userId) {
        boolean alreadyMember = groupMemberRepository
                .existsByGroupIdAndUserId(groupId, userId);

        if (!alreadyMember) {
            groupMemberRepository.save(GroupMember.builder()
                    .groupId(groupId)
                    .userId(userId)
                    .role("MEMBER")
                    .build());
        }
    }

    public void removeGroupMember(String groupId, String userId) {
        List<GroupMember> members = groupMemberRepository
                .findByGroupId(groupId);
        members.stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .ifPresent(m -> groupMemberRepository.delete(m));
    }

    public List<Group> getUserGroups(String userId) {
        return groupMemberRepository.findByUserId(userId)
                .stream()
                .map(GroupMember::getGroupId)
                .map(groupId -> groupRepository.findById(groupId).orElse(null))
                .filter(g -> g != null && !g.isArchived())
                .toList();
    }

    public List<GroupMember> getGroupMembers(String groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }

    private String normalizeGroupType(String groupType) {
        if (groupType == null || groupType.isBlank()) {
            return "friends";
        }
        String normalized = groupType.trim().toLowerCase();
        return switch (normalized) {
            case "trip", "flat", "office", "friends" -> normalized;
            default -> "friends";
        };
    }

    public boolean isGroupMember(String groupId, String userId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }
}
