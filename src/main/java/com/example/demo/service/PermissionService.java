package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Permission;

public interface PermissionService {

    Permission createPermission(Permission permission);

    List<Permission> getAllPermissions();

    Permission getPermissionById(Long id);
}