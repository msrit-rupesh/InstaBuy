package com.example.userservice.service;

import com.example.userservice.model.Role;
import com.example.userservice.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository)
    {
        this.roleRepository=roleRepository;
    }

    public Role getRoleById(short id)
    {
        return roleRepository.findById(id).orElse(null);
    }
}
