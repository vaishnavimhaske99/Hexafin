package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Permission;
import com.example.demo.repository.PermissionRepository;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(
            PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public Permission createPermission(Permission permission) {

        if (permissionRepository.existsByName(permission.getName())) {
            throw new RuntimeException(
                    "Permission already exists: "
                    + permission.getName());
        }

        return permissionRepository.save(permission);
    }

    @Override
    public List<Permission> getAllPermissions() {

        return permissionRepository.findAll();
    }

    @Override
    public Permission getPermissionById(Long id) {

        return permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Permission not found with id: " + id));
    }
}