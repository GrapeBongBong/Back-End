package com.example.capstone.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Table(name = "anonymous_post")
public class AnonymousPost extends Post {

}
