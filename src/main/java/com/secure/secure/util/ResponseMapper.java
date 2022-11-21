package com.secure.secure.util;

public interface ResponseMapper {

    default ResponseObject errorResponse(CustomException ce){
        var response = new ResponseObject();
        response.setStatus("ERROR");
        response.setResponse(ce.getMessage());
        return response;
    }

    default ResponseObject successResponse(Object o){
        var response = new ResponseObject();
        response.setStatus("SUCCESS");
        response.setResponse(o);
        return response;
    }

}
