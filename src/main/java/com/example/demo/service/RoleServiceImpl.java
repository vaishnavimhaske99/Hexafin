package com.example.demo.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleServiceImpl(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository) {

        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public Role createRole(Role role) {

        if (roleRepository.existsByName(role.getName())) {
            throw new RuntimeException(
                    "Role already exists: " + role.getName());
        }

        return roleRepository.save(role);
    }

    @Override
    public List<Role> getAllRoles() {

        return roleRepository.findAll();
    }

    @Override
    public Role getRoleById(Long id) {

        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Role not found with id: " + id));
    }

    @Override
    public Role assignPermissions(
            Long roleId,
            Set<Long> permissionIds) {

        Role role = getRoleById(roleId);

        Set<Permission> permissions = new HashSet<>();

        for (Long permissionId : permissionIds) {

            Permission permission =
                    permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new RuntimeException(
                            "Permission not found with id: "
                            + permissionId));

            permissions.add(permission);
        }

        role.setPermissions(permissions);

        return roleRepository.save(role);
    }
}