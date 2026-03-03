package com.laitsneo.whitelbl.dto.Client;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ClientLoginResponse {

    private String email;
    private String userId;

}
