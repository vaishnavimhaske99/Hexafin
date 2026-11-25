
package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.UserLocation;

public interface UserLocationRepository
        extends JpaRepository<UserLocation, Long> {

    Optional<UserLocation> findByUserId(Long userId);
}