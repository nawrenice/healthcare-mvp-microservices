package com.healthcare.userservice.service;

import com.healthcare.userservice.dto.request.CreateUserProfileRequest;
import com.healthcare.userservice.dto.request.UpdateUserProfileRequest;
import com.healthcare.userservice.dto.response.UserProfileResponse;
import com.healthcare.userservice.entity.UserProfile;
import com.healthcare.userservice.exception.DuplicateUserProfileException;
import com.healthcare.userservice.exception.UserProfileNotFoundException;
import com.healthcare.userservice.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileResponse getProfileByAuthUserId(Long authUserId){
        UserProfile userProfile =  userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(()-> new UserProfileNotFoundException("User profile not found with authUserId : " +authUserId));
        return mapToResponse(userProfile);

    }



    public UserProfileResponse updateProfile(Long authUserId, UpdateUserProfileRequest request){
        UserProfile existingProfile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(()-> new UserProfileNotFoundException("User profile not found with authUserId : " +authUserId));

        existingProfile.setName(request.getName());
        existingProfile.setPhone(request.getPhone());
        existingProfile.setAddress(request.getAddress());
        existingProfile.setDateOfBirth(request.getDateOfBirth());
        existingProfile.setGender(request.getGender());

        UserProfile savedProfile = userProfileRepository.save(existingProfile);

        return mapToResponse(savedProfile);
    }

    public UserProfileResponse createProfile(CreateUserProfileRequest request){
        if(userProfileRepository.existsByAuthUserId(request.getAuthUserId())){
            throw new DuplicateUserProfileException("User profile already exists for authUserId : " +request.getAuthUserId());
        }

        UserProfile userProfile = UserProfile.builder()
                .authUserId(request.getAuthUserId())
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .build();

        UserProfile savedProfile = userProfileRepository.save(userProfile);

        return mapToResponse(savedProfile);
    }


    private UserProfileResponse mapToResponse(UserProfile userProfile) {

        return UserProfileResponse.builder()
                .id(userProfile.getId())
                .authUserId(userProfile.getAuthUserId())
                .name(userProfile.getName())
                .phone(userProfile.getPhone())
                .address(userProfile.getAddress())
                .dateOfBirth(userProfile.getDateOfBirth())
                .gender(userProfile.getGender())
                .createdAt(userProfile.getCreatedAt())
                .updatedAt(userProfile.getUpdatedAt())
                .build();
    }
}
