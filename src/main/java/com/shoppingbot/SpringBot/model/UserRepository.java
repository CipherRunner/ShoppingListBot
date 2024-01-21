package com.shoppingbot.SpringBot.model;

import jakarta.persistence.Entity;

import org.springframework.data.repository.CrudRepository;


public interface UserRepository extends CrudRepository<User, Long> {

}
