package com.api.bigu.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Integer userId;

    private String fullName;

    private String sex;

    private String email;

    private String phoneNumber;

    private String matricula;


}
