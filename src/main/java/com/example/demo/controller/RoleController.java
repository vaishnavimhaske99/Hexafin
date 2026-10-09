package com.example.demo.controller;

import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Role;
import com.example.demo.service.RoleService;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public Role createRole(
            @RequestBody Role role) {

        return roleService.createRole(role);
    }

    @GetMapping
    public List<Role> getAllRoles() {

        return roleService.getAllRoles();
    }

    @GetMapping("/{id}")
    public Role getRoleById(
            @PathVariable Long id) {

        return roleService.getRoleById(id);
    }

    @PutMapping("/{roleId}/permissions")
    public Role assignPermissions(
            @PathVariable Long roleId,
            @RequestBody Set<Long> permissionIds) {

        return roleService.assignPermissions(
                roleId, permissionIds);
    }
}