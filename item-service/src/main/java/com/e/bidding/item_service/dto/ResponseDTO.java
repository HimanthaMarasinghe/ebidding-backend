package com.e.bidding.item_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO<T> {
    private boolean success;
    private T data;
    private String message;

    public ResponseDTO(T b, T o, T s) {
    }
}
