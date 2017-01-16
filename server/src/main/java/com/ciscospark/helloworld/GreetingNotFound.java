package com.ciscospark.helloworld;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="No such greeting")
public class GreetingNotFound extends RuntimeException
{

}
