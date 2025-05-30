package id.ac.ui.cs.advprog.beprofile.controller;

import id.ac.ui.cs.advprog.beprofile.dto.ApiResponseDto;
import id.ac.ui.cs.advprog.beprofile.dto.DoctorResponseDto;
import id.ac.ui.cs.advprog.beprofile.dto.DoctorSearchRequestDto;
import id.ac.ui.cs.advprog.beprofile.enums.Speciality;
import id.ac.ui.cs.advprog.beprofile.service.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DoctorControllerTest {

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private DoctorController doctorController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void searchDoctors_withParameters_shouldReturnPagedDoctors() {
        // Prepare sample response
        DoctorResponseDto doctor = DoctorResponseDto.builder()
                .id("1")
                .name("Dr. John")
                .speciality(Speciality.DOKTER_UMUM)
                .workingSchedules(Collections.emptyList())
                .build();
        Page<DoctorResponseDto> page = new PageImpl<>(List.of(doctor));

        // Mock service
        when(doctorService.searchDoctors(any(DoctorSearchRequestDto.class))).thenReturn(page);

        // Call controller with all params
        ResponseEntity<ApiResponseDto<Page<DoctorResponseDto>>> response = doctorController
                .searchDoctors(
                        "John",
                        "Dokter Umum",
                        "Mon-Fri",
                        "MONDAY",
                        "08:00",
                        "12:00",
                        1,
                        5
                );

        // Verify status and body
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponseDto<Page<DoctorResponseDto>> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("Doctors retrieved successfully", body.getMessage());
        assertSame(page, body.getData());

        // Verify service was called with correct DTO
        ArgumentCaptor<DoctorSearchRequestDto> captor = ArgumentCaptor.forClass(DoctorSearchRequestDto.class);
        verify(doctorService).searchDoctors(captor.capture());
        DoctorSearchRequestDto dto = captor.getValue();
        assertEquals("John", dto.getName());
        assertEquals(Speciality.DOKTER_UMUM, dto.getSpeciality());
        assertEquals("Mon-Fri", dto.getWorkingSchedule());
        assertEquals(DayOfWeek.MONDAY, dto.getWorkingDay());
        assertEquals(LocalTime.parse("08:00"), dto.getStartTime());
        assertEquals(LocalTime.parse("12:00"), dto.getEndTime());
        assertEquals(1, dto.getPage());
        assertEquals(5, dto.getSize());
    }

    @Test
    void getAllDoctors_shouldReturnDefaultPagedDoctors() {
        // Prepare sample response
        DoctorResponseDto doctor = DoctorResponseDto.builder()
                .id("2")
                .name("Dr. Jane")
                .speciality(Speciality.SPESIALIS_ANAK)
                .workingSchedules(Collections.emptyList())
                .build();
        Page<DoctorResponseDto> page = new PageImpl<>(List.of(doctor));

        when(doctorService.searchDoctors(any(DoctorSearchRequestDto.class))).thenReturn(page);

        // Call controller with defaults
        ResponseEntity<ApiResponseDto<Page<DoctorResponseDto>>> response = doctorController.getAllDoctors(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponseDto<Page<DoctorResponseDto>> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("All doctors retrieved successfully", body.getMessage());
        assertSame(page, body.getData());

        ArgumentCaptor<DoctorSearchRequestDto> captor = ArgumentCaptor.forClass(DoctorSearchRequestDto.class);
        verify(doctorService).searchDoctors(captor.capture());
        DoctorSearchRequestDto dto = captor.getValue();
        assertNull(dto.getName());
        assertNull(dto.getSpeciality());
        assertNull(dto.getWorkingSchedule());
        assertNull(dto.getWorkingDay());
        assertNull(dto.getStartTime());
        assertNull(dto.getEndTime());
        assertEquals(0, dto.getPage());
        assertEquals(10, dto.getSize());
    }

    @Test
    void getDoctorById_shouldReturnDoctorDetails() {
        // Prepare sample response
        DoctorResponseDto doctor = DoctorResponseDto.builder()
                .id("3")
                .name("Dr. Smith")
                .speciality(Speciality.SPESIALIS_KULIT)
                .workingSchedules(Collections.emptyList())
                .build();

        when(doctorService.getDoctorById("3")).thenReturn(doctor);

        ResponseEntity<ApiResponseDto<DoctorResponseDto>> response = doctorController.getDoctorById("3");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponseDto<DoctorResponseDto> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.getStatus());
        assertEquals("Doctor details retrieved successfully", body.getMessage());
        assertNotNull(body.getData());
        assertEquals("3", body.getData().getId());
        assertEquals("Dr. Smith", body.getData().getName());
        assertEquals(Speciality.SPESIALIS_KULIT, body.getData().getSpeciality());
    }

    @Test
    void searchDoctors_invalidSpeciality_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                doctorController.searchDoctors(
                        "John", "InvalidSpeciality", null, null, null, null, 0, 10
                )
        );
        assertEquals("Invalid speciality: InvalidSpeciality", exception.getMessage());
    }

    @Test
    void searchDoctors_invalidWorkingDay_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                doctorController.searchDoctors(
                        null, null, null, "FUNDAY", null, null, 0, 10
                )
        );
        assertEquals("Invalid working day: FUNDAY", exception.getMessage());
    }

    @Test
    void searchDoctors_invalidStartTime_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                doctorController.searchDoctors(
                        null, null, null, null, "invalid-time", null, 0, 10
                )
        );
        assertEquals("Invalid start time format: invalid-time. Expected format: HH:mm", exception.getMessage());
    }

    @Test
    void searchDoctors_invalidEndTime_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                doctorController.searchDoctors(
                        null, null, null, null, null, "25:61", 0, 10
                )
        );
        assertEquals("Invalid end time format: 25:61. Expected format: HH:mm", exception.getMessage());
    }
}
