package com.healthcare.userservice.controller;

import com.healthcare.userservice.dto.request.CreateUserProfileRequest;
import com.healthcare.userservice.dto.request.UpdateUserProfileRequest;
import com.healthcare.userservice.dto.response.UserProfileResponse;
import com.healthcare.userservice.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping
    public UserProfileResponse createProfile(@Valid @RequestBody CreateUserProfileRequest request){
        return userProfileService.createProfile(request);
    }

    @GetMapping("/{authUserId}")
    public UserProfileResponse getProfile(@PathVariable Long authUserId){
        return userProfileService.getProfileByAuthUserId(authUserId);
    }

    @PutMapping("/{authUserId}")
    public UserProfileResponse updateProfile(
            @PathVariable Long authUserId,
            @Valid @RequestBody UpdateUserProfileRequest request
    ){
        return userProfileService.updateProfile(authUserId, request);
    }


}
