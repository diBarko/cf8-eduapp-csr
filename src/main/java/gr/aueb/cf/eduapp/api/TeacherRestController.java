package gr.aueb.cf.eduapp.api;

import gr.aueb.cf.eduapp.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.eduapp.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.eduapp.core.exceptions.ValidationException;
import gr.aueb.cf.eduapp.core.filters.Paginated;
import gr.aueb.cf.eduapp.core.filters.TeacherFilters;
import gr.aueb.cf.eduapp.dto.TeacherInsertDTO;
import gr.aueb.cf.eduapp.dto.TeacherReadOnlyDTO;
import gr.aueb.cf.eduapp.service.ITeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TeacherRestController {

    private final ITeacherService teacherService;
    private static final Logger logger = LoggerFactory.getLogger(TeacherRestController.class);

//    @PostMapping(value = "/teachers")

@PostMapping(value = "/teachers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<TeacherReadOnlyDTO> saveTeacher(
        @Valid @RequestPart(name = "teacher") TeacherInsertDTO teacherInsertDTO,
        BindingResult bindingResult,
        @Nullable @RequestPart(value = "amkaFile", required = false) MultipartFile amkaFile)
        throws AppObjectAlreadyExists, AppObjectInvalidArgumentException, IOException, ValidationException {
    logger.info("saveTeacher method called");
    logger.debug("Received file: {} - {} - {} bytes",
            amkaFile != null ? amkaFile.getOriginalFilename() : "null",
            amkaFile != null ? amkaFile.getContentType() : "null",
            amkaFile != null ? amkaFile.getSize() : 0);

    if (amkaFile != null) {
        logger.info("File received: {}", amkaFile.getOriginalFilename());
    } else {
        logger.info("No file received");
    }


        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        TeacherReadOnlyDTO teacherReadOnlyDTO = teacherService.saveTeacher(teacherInsertDTO, amkaFile);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()   //  /teachers
            .path("/{id}")      //  appends  /{id}
            .buildAndExpand(teacherReadOnlyDTO.getId())
            .toUri();

        return ResponseEntity
                .created(location)
                .body(teacherReadOnlyDTO);
    }

    @GetMapping("/teachers")
    public ResponseEntity<Page<TeacherReadOnlyDTO>> getPaginatedTeachers(
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size
    ) {
       Page<TeacherReadOnlyDTO> teachersPage = teacherService.getPaginatedTeachers(page, size);
       return ResponseEntity.ok(teachersPage);
    }

    public ResponseEntity<Paginated<TeacherReadOnlyDTO>> getFilteredAndPaginatedTeachers(
            @Nullable @RequestBody TeacherFilters filters) {
        if (filters == null) filters = TeacherFilters.builder().build();        // create empty filter to avoid null
        Paginated<TeacherReadOnlyDTO> dtoPaginated = teacherService.getTeachersFilteredPaginated(filters);
        return ResponseEntity.ok(dtoPaginated);
    }
}