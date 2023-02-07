package com.fkp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: fkp
 * @time: 2023-02-05 21:41:04
 * @description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private Long id;

    private String city;

    private String name;

    private Long carId;

    private static final long serialVersionUID = 1L;
}
