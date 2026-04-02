package com.healthcare.authservice.exception;



/*
 *Custom exception to indicate a user already exists.
 * Useful for clean error handling and interview discussion.
 *  */
public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException(String message){
        super(message); //Call RuntimeException constructor
    }

}
