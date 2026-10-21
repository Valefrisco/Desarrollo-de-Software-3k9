package com.tup.programacion3.DTOs;

import java.time.LocalDateTime;


public record ErrorDTO(int status, String mensaje, LocalDateTime timestamp) {
}
