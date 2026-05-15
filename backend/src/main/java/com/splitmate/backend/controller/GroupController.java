package com.splitmate.backend.controller;

import com.splitmate.backend.model.Group;
import com.splitmate.backend.model.GroupMember;
import com.splitmate.backend.security.JwtUtil;
import com.splitmate.backend.service.GroupService;
import com.splitmate.backend.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final SettlementService settlementService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createGroup(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        try {
            String userId = extractUserId(authHeader);
            String name = body.get("name");

            if (name == null || name.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Group name is required"));
            }

            Group group = groupService.createGroup(name, userId);
            return ResponseEntity.ok(group);

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserGroups(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserId(authHeader);
            List<Group> groups = groupService.getUserGroups(userId);
            return ResponseEntity.ok(groups);

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroup(
            @PathVariable String groupId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserId(authHeader);
            Group group = verifyGroupAccess(groupId, userId);

            return ResponseEntity.ok(Map.of(
                    "group", group,
                    "members", groupService.getGroupMembers(groupId)));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized or group not found"));
        }
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<?> updateGroup(
            @PathVariable String groupId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        try {
            String userId = extractUserId(authHeader);
            Group group = verifyGroupAccess(groupId, userId);

            String name = body.getOrDefault("name", group.getName());
            String photoUrl = body.getOrDefault("photoUrl", group.getPhotoUrl());

            Group updated = groupService.updateGroup(groupId, name, photoUrl);
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(
            @PathVariable String groupId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserId(authHeader);
            Group group = verifyGroupAccess(groupId, userId);

            groupService.deleteGroup(groupId);
            return ResponseEntity.ok(Map.of("message", "Group archived successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> joinGroup(
            @PathVariable String groupId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        try {
            String userId = extractUserId(authHeader);
            groupService.joinGroup(groupId, userId);

            return ResponseEntity.ok(Map.of(
                    "message", "Successfully joined group",
                    "groupId", groupId));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }
    }

    @PostMapping("/{groupId}/invite-link")
    public ResponseEntity<?> generateInviteLink(
            @PathVariable String groupId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserId(authHeader);
            verifyGroupAccess(groupId, userId);

            String link = groupService.generateInviteLink(groupId);
            return ResponseEntity.ok(Map.of("inviteLink", link));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(
            @PathVariable String groupId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserId(authHeader);
            verifyGroupAccess(groupId, userId);

            List<GroupMember> members = groupService.getGroupMembers(groupId);
            return ResponseEntity.ok(members);

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }
    }

    @GetMapping("/{groupId}/settlements")
    public ResponseEntity<?> getGroupSettlements(
            @PathVariable String groupId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserId(authHeader);
            verifyGroupAccess(groupId, userId);

            return ResponseEntity.ok(Map.of(
                    "groupId", groupId,
                    "balances", settlementService.calculateNetBalances(groupId),
                    "settlements", settlementService.getMinimizedSettlements(groupId)));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }
    }

    private String extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            throw new RuntimeException("Invalid or expired token");
        }
        return jwtUtil.extractUserId(token);
    }

    private Group verifyGroupAccess(String groupId, String userId) {
        List<Group> userGroups = groupService.getUserGroups(userId);
        return userGroups.stream()
                .filter(g -> g.getId().equals(groupId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Group not accessible"));
    }
}
