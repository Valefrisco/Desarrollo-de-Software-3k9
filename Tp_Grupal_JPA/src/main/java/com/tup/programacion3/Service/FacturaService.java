package com.tup.programacion3.Service;

import com.tup.programacion3.DTOs.FacturaReporteDTO;
import com.tup.programacion3.Repository.FacturaReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacturaService {

    private final FacturaReporteRepository repository;

    @Transactional(readOnly = true)
    public List<FacturaReporteDTO> buscarFacturasFiltradas(
            LocalDateTime desde, LocalDateTime hasta, String estado, Double montoMinimo) {
        return repository.buscarFacturasFiltradas(desde, hasta, estado, montoMinimo);
    }
}