package com.playtika.gamesessions.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OverAllocatedTimeException extends RuntimeException {

    public OverAllocatedTimeException() {
        super("Warning! Starting game session but it's beyond daily allocated time");
    }

    public OverAllocatedTimeException(String message) {
        super(message);
    }

}
