package com.example.demo.service;

import java.util.List;
import java.util.Set;

import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;

public interface RoleService {

    Role createRole(Role role);

    List<Role> getAllRoles();

    Role getRoleById(Long id);

    Role assignPermissions(Long roleId, Set<Long> permissionIds);
}