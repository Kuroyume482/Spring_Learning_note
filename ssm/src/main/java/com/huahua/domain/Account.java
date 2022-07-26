package com.huahua.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Account {
    private Integer id;
    private String name;
    private Double money;
}
