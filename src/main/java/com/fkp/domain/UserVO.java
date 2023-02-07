package com.fkp.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserVO implements Serializable {
    private Long id;

    private String city;

    private String name;

    private String carName;

    private Integer carPrice;

    private static final long serialVersionUID = 1L;
}
