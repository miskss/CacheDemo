package com.example.cache.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author peter
 * date: 2019-10-16 09:56
 **/
@Data
@AllArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 6119134703242129065L;

    private Integer id;
    private String name;

    private Integer age;
}
