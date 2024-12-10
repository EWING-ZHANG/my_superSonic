package com.tencent.supersonic.common.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data

@NoArgsConstructor
@AllArgsConstructor
public class UserVO implements Serializable {

    private String id;

    private String name;

    private String displayName;

    private String email;

    private Integer isAdmin;
}
