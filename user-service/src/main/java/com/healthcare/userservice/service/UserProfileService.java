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

        //Update only if value is provided in request
        if(request.getName() != null) {
            existingProfile.setName(request.getName());
        }
        if(request.getPhone() != null) {
            existingProfile.setPhone(request.getPhone());
        }
        if(request.getAddress() != null) {
            existingProfile.setAddress(request.getAddress());
        }
        if(request.getDateOfBirth() != null) {
            existingProfile.setDateOfBirth(request.getDateOfBirth());
        }
        if(request.getGender() != null) {
            existingProfile.setGender(request.getGender());
        }

        UserProfile savedProfile = userProfileRepository.save(existingProfile);

        return mapToResponse(savedProfile);
    }

    public UserProfileResponse createProfile(Long authUserId, CreateUserProfileRequest request){
        //Check if profile already exists for this auth user
        if(userProfileRepository.existsByAuthUserId(authUserId)){
            throw new DuplicateUserProfileException("User profile already exists for authUserId : " +authUserId);
        }

        //create new entity
        UserProfile userProfile = UserProfile.builder()
                .authUserId(authUserId)
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .build();

        //save to DB
        UserProfile savedProfile = userProfileRepository.save(userProfile);

        //convert to response DTO
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
