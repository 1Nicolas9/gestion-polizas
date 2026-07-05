package com.bolivar.gestionpolizas.service;

import com.bolivar.gestionpolizas.config.exception.NegocioException;
import com.bolivar.gestionpolizas.config.exception.RecursoNoEncontradoException;
import com.bolivar.gestionpolizas.model.*;
import com.bolivar.gestionpolizas.repository.PolizaRepository;
import com.bolivar.gestionpolizas.repository.RiesgoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolizaServiceTest {

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private RiesgoRepository riesgoRepository;

    @Mock
    private CoreMockService coreMockService;

    @InjectMocks
    private PolizaService polizaService;

    private Poliza polizaEjemplo;

    @BeforeEach
    void setUp() {
        polizaEjemplo = new Poliza();
        polizaEjemplo.setId(1L);
        polizaEjemplo.setTipo(TipoPoliza.INDIVIDUAL);
        polizaEjemplo.setEstado(EstadoPoliza.ACTIVA);
        polizaEjemplo.setCanonMensual(new BigDecimal("100000"));
        polizaEjemplo.setPrima(new BigDecimal("1200000"));
    }

    @Nested
    @DisplayName("Pruebas para listarPolizas")
    class ListarPolizasTests {

        @Test
        @DisplayName("Debe listar con filtros válidos")
        void debeListarConFiltrosValidos() {
            when(polizaRepository.findByFiltros(TipoPoliza.INDIVIDUAL, EstadoPoliza.ACTIVA))
                    .thenReturn(List.of(polizaEjemplo));

            List<Poliza> resultado = polizaService.listarPolizas("INDIVIDUAL", "ACTIVA");

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            verify(polizaRepository, times(1)).findByFiltros(TipoPoliza.INDIVIDUAL, EstadoPoliza.ACTIVA);
        }

        @Test
        @DisplayName("Debe lanzar NegocioException por tipo inválido")
        void debeLanzarExceptionPorTipoInvalido() {
            NegocioException exception = assertThrows(NegocioException.class, () -> 
                polizaService.listarPolizas("INVALIDO_TIPO", "ACTIVA")
            );
            assertTrue(exception.getMessage().contains("Tipo de póliza inválido"));
        }

        @Test
        @DisplayName("Debe lanzar NegocioException por estado inválido")
        void debeLanzarExceptionPorEstadoInvalido() {
            NegocioException exception = assertThrows(NegocioException.class, () -> 
                polizaService.listarPolizas("INDIVIDUAL", "INVALIDO_ESTADO")
            );
            assertTrue(exception.getMessage().contains("Estado de póliza inválido"));
        }
    }

    @Nested
    @DisplayName("Pruebas para renovarPoliza")
    class RenovarPolizaTests {

        @Test
        @DisplayName("Debe renovar exitosamente incrementando valores y notificando al CORE")
        void debeRenovarExitosamente() {
            BigDecimal ipc = new BigDecimal("0.05"); // 5%
            when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaEjemplo));
            when(polizaRepository.save(any(Poliza.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Poliza resultado = polizaService.renovarPoliza(1L, ipc);

            assertNotNull(resultado);
            assertEquals(EstadoPoliza.RENOVADA, resultado.getEstado());
            assertEquals(new BigDecimal("105000.00"), resultado.getCanonMensual()); // 100k * 1.05
            assertEquals(new BigDecimal("1260000.00"), resultado.getPrima());       // 1.2M * 1.05
            
            verify(coreMockService, times(1)).notificarEvento("ACTUALIZACION", 1L);
            verify(polizaRepository, times(1)).save(polizaEjemplo);
        }

        @Test
        @DisplayName("Debe lanzar RecursoNoEncontradoException si la póliza no existe")
        void debeLanzarExceptionSiPolizaNoExiste() {
            when(polizaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(RecursoNoEncontradoException.class, () -> 
                polizaService.renovarPoliza(99L, new BigDecimal("0.05"))
            );
        }

        @Test
        @DisplayName("Debe lanzar NegocioException si la póliza ya está CANCELADA")
        void debeLanzarExceptionSiEstaCancelada() {
            polizaEjemplo.setEstado(EstadoPoliza.CANCELADA);
            when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaEjemplo));

            NegocioException exception = assertThrows(NegocioException.class, () -> 
                polizaService.renovarPoliza(1L, new BigDecimal("0.05"))
            );
            assertEquals("No se puede renovar una póliza que se encuentra en estado CANCELADA", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar NegocioException si el IPC es menor o igual a cero")
        void debeLanzarExceptionSiIpcInvalido() {
            when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaEjemplo));

            assertThrows(NegocioException.class, () -> polizaService.renovarPoliza(1L, BigDecimal.ZERO));
            assertThrows(NegocioException.class, () -> polizaService.renovarPoliza(1L, new BigDecimal("-0.01")));
        }
    }

    @Nested
    @DisplayName("Pruebas para cancelarPoliza")
    class CancelarPolizaTests {

        @Test
        @DisplayName("Debe cancelar la póliza y todos los riesgos asociados de forma en cascada")
        void debeCancelarPolizaYRiesgos() {
            Riesgo riesgo = new Riesgo();
            riesgo.setId(10L);
            riesgo.setEstado(EstadoRiesgo.ACTIVO);

            when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaEjemplo));
            when(riesgoRepository.findByPolizaId(1L)).thenReturn(List.of(riesgo));

            Poliza resultado = polizaService.cancelarPoliza(1L);

            assertEquals(EstadoPoliza.CANCELADA, resultado.getEstado());
            assertEquals(EstadoRiesgo.CANCELADO, riesgo.getEstado());
            
            verify(polizaRepository, times(1)).save(polizaEjemplo);
            verify(riesgoRepository, times(1)).save(riesgo);
            verify(coreMockService, times(1)).notificarEvento("ACTUALIZACION", 1L);
        }
    }

    @Nested
    @DisplayName("Pruebas para agregarRiesgoAPoliza")
    class AgregarRiesgoTests {

        @Test
        @DisplayName("Debe agregar riesgo si la póliza es COLECTIVA y está ACTIVA")
        void debeAgregarRiesgoExitosamente() {
            polizaEjemplo.setTipo(TipoPoliza.COLECTIVA);
            Riesgo nuevoRiesgo = new Riesgo();
            
            when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaEjemplo));
            when(riesgoRepository.save(any(Riesgo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Riesgo resultado = polizaService.agregarRiesgoAPoliza(1L, nuevoRiesgo);

            assertNotNull(resultado);
            assertEquals(1L, resultado.getPolizaId());
            assertEquals(EstadoRiesgo.ACTIVO, resultado.getEstado());
            verify(riesgoRepository, times(1)).save(nuevoRiesgo);
        }

        @Test
        @DisplayName("Debe lanzar NegocioException si se intenta añadir riesgo a una póliza INDIVIDUAL")
        void debeLanzarExceptionSiEsIndividual() {
            polizaEjemplo.setTipo(TipoPoliza.INDIVIDUAL);
            Riesgo nuevoRiesgo = new Riesgo();

            when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaEjemplo));

            NegocioException exception = assertThrows(NegocioException.class, () -> 
                polizaService.agregarRiesgoAPoliza(1L, nuevoRiesgo)
            );
            assertTrue(exception.getMessage().contains("Solo se pueden agregar riesgos a pólizas de tipo COLECTIVA"));
        }
    }
}