package com.simplechat.util.api;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @author Mohsen Jahanshahi
 */
public class ResponseEntityGenerator {

    private ResponseEntityGenerator(){}

    public static ResponseEntity<String> createSuccesResponseEntity(JSONObject data) {
        ApiResultJSON apiResult = new ApiResultJSON();
        apiResult.setData(data);
        apiResult.setResultStatus(ResultStatus.SUCCESS);

        return new ResponseEntity<>(apiResult.getResult(), HttpStatus.OK);
    }

    public static ResponseEntity<String> createSuccesResponseEntity() {
        ApiResultJSON apiResult = new ApiResultJSON();
        apiResult.setResultStatus(ResultStatus.SUCCESS);

        return new ResponseEntity<>(apiResult.getResult(), HttpStatus.OK);
    }

    public static ResponseEntity<String> createErrorResponseEntity(ResultStatus resultStatus) {

        ApiResultJSON apiResult = new ApiResultJSON();
        apiResult.setResultStatus(resultStatus);
        return new ResponseEntity<>(apiResult.getResult(), HttpStatus.OK);
    }

    public static ResponseEntity<String> createErrorResponseEntity(ResultStatus resultStatus, JSONObject errors) {

        ApiResultJSON apiResult = new ApiResultJSON();
        apiResult.setResultStatus(resultStatus);
        apiResult.setErrors(errors);
        return new ResponseEntity<>(apiResult.getResult(), HttpStatus.OK);
    }
}
