package com.laitsneo.mipPay.response;

import lombok.Data;

@Data
public class TrexoCheckStatusResponse {
	 private String code;
	    private String response;
	    private String message;
	    private Object data;
	    private String timestamp;
}
