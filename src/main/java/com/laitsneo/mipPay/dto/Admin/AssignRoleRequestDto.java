package com.laitsneo.mipPay.dto.Admin;

import lombok.Data;

@Data
public class AssignRoleRequestDto {

    private String adminId;
    private String userName;
    private String email;
    private String roleId;
    private String permissions;
    private String status;
}
