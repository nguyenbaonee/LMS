package com.example.LMS.service;

import com.example.LMS.Exception.AppException;
import com.example.LMS.Exception.ErrorCode;
import com.example.LMS.dto.Request.StudentQuery;
import com.example.LMS.dto.Request.StudentRequest;
import com.example.LMS.dto.Request.StudentUpdate;
import com.example.LMS.dto.Response.StudentResponse;
import com.example.LMS.dto.dtoProjection.ImageDTO;
import com.example.LMS.dto.dtoProjection.StudentAvatarDTO;
import com.example.LMS.dto.dtoProjection.StudentDTO;
import com.example.LMS.entity.Image;
import com.example.LMS.entity.Student;
import com.example.LMS.enums.ImageType;
import com.example.LMS.enums.ObjectType;
import com.example.LMS.enums.Status;
import com.example.LMS.mapper.StudentMap;
import com.example.LMS.repo.ImageRepo;
import com.example.LMS.repo.StudentRepo;
import jakarta.servlet.http.HttpServletResponse;
import org.jxls.util.JxlsHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.jxls.common.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudentService {

    StudentRepo studentRepo;
    StudentMap studentMap;
    FileStorageService fileStorageService;
    ImageRepo imageRepo;

    public StudentService(StudentRepo studentRepo, StudentMap studentMap,
                          FileStorageService fileStorageService, ImageRepo imageRepo) {
        this.studentRepo = studentRepo;
        this.studentMap = studentMap;
        this.fileStorageService = fileStorageService;
        this.imageRepo = imageRepo;
    }

    @Transactional(rollbackFor = Exception.class)
    public StudentResponse createStd(StudentRequest studentRequest, List<MultipartFile> images)
            throws IOException {
        if(studentRepo.existsByEmail(studentRequest.getEmail())){
            throw new AppException(ErrorCode.EMAILEXISTS);
        }
        Student student = studentMap.toStudent(studentRequest);
        studentRepo.save(student);
        if (images == null || images.isEmpty()) {
            return studentMap.toStdResponse(student);
        }

        List<Image> avatar = new ArrayList<>();
        List<String> savedFilePaths = new ArrayList<>();

        try{
            if(images != null && !images.isEmpty()){
                boolean first = true;
                for(MultipartFile file : images) {
                    ImageDTO imageDTO = fileStorageService.save(file, ObjectType.STUDENT, ImageType.IMAGE);
                    Image image = new Image();
                    image.setUrl(imageDTO.getUrl());
                    image.setFileName(imageDTO.getFilename());
                    image.setType(ImageType.IMAGE);
                    image.setObjectType(ObjectType.STUDENT);
                    image.setObjectId(student.getId());
                    image.setPrimary(first);
                    first = false;
                    avatar.add(image);
                    savedFilePaths.add(imageDTO.getUrl());
                }
            }
            imageRepo.saveAll(avatar);
            student.setAvatar(avatar);
            studentRepo.save(student);
            return studentMap.toStdResponse(student);
        }
        catch (Exception e){
            fileStorageService.deleteFiles(savedFilePaths);
            throw e;
        }
    }

    public Page<StudentResponse> searchStudent(int page, int size, String name, String email, Status status){
        if (name != null && !name.isBlank()) {
            name = "%" + name.replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_")
                    .toLowerCase() + "%";
        } else {
            name = null;
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Long> studentIds = studentRepo.searchIds(pageable, name, email,status);
        if (studentIds.getTotalElements() == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<Long> ids = studentIds.getContent();
        List<StudentAvatarDTO> studentDTOS = studentRepo.findStudentAvatars(ids);
        List<StudentDTO> studentDTOList = studentRepo.findStudentDTOsByIds(ids, status);
        Map<Long, List<Image>> studentMapId = studentDTOS.stream()
                .collect(Collectors.groupingBy(
                        StudentAvatarDTO::getId,
                        Collectors.mapping(StudentAvatarDTO::getImage, Collectors.toList())
                ));
        studentDTOList.forEach(studentDTO -> {studentDTO.setAvatar(studentMapId
                .getOrDefault(studentDTO.getId(), List.of()));});
        List<StudentResponse> studentResponseList = studentMap.toStdResponseFromDTOs(studentDTOList);
        return new PageImpl<>(studentResponseList, pageable, studentIds.getTotalElements());
    }

    public StudentResponse getStudentDetail(Long id,Status status) {

        // Lấy avatar
        List<StudentAvatarDTO> avatarDTOs = studentRepo.findStudentAvatars(List.of(id));
        if(avatarDTOs.isEmpty()){
            throw new AppException(ErrorCode.STUDENTNOTFOUND);
        }
        // Lấy thông tin student DTO
        List<StudentDTO> studentDTOList = studentRepo.findStudentDTOsByIds(List.of(id), status);

        // Gom avatar theo id
        Map<Long, List<Image>> avatarMap = avatarDTOs.stream()
                .collect(Collectors.groupingBy(
                        StudentAvatarDTO::getId,
                        Collectors.mapping(StudentAvatarDTO::getImage, Collectors.toList())
                ));

        // Set avatar vào DTO
        StudentDTO dto = studentDTOList.get(0);
        dto.setAvatar(avatarMap.getOrDefault(id, List.of()));

        return studentMap.toStdResponseFromDTO(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    public StudentResponse updateStudent(Long id, StudentUpdate studentUpdate, List<MultipartFile> images,
                                         List<Long> deleteAvatarsId, Long mainAvatarId) throws IOException {
        //check theo id va Status
        Student student = studentRepo.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENTNOTFOUND));

        studentMap.updateStudent(studentUpdate, student);

        //Xoa mem cac anh co trong list xoa
        if(deleteAvatarsId != null && !deleteAvatarsId.isEmpty()) {
            List<Image> imagesDelete = imageRepo.findAllByIdInAndObjectIdAndStatus(deleteAvatarsId, student.getId(), Status.ACTIVE);
            if(imagesDelete.size() != deleteAvatarsId.size()){
                throw new AppException(ErrorCode.AVATAR_DELETE_NOT_FOUND);
            }
            imagesDelete.forEach(image -> {image.setStatus(Status.DELETED);});
            imageRepo.saveAll(imagesDelete);
        }

        List<Image> avatarActive = imageRepo.findByObjectIdAndStatus(student.getId(), Status.ACTIVE);
        //xu ly avatar hien thi
        if(mainAvatarId != null){
            boolean exists = avatarActive.stream()
                    .anyMatch(img -> img.getId().equals(mainAvatarId));
            System.out.println("exists: " + exists);
            if (!exists) {
                throw new AppException(ErrorCode.AVATAR_MAIN_NOT_FOUND);
            }
            for(Image img : avatarActive) {
                img.setPrimary(img.getId().equals(mainAvatarId));
            }
            imageRepo.saveAll(avatarActive);
        } else if(!avatarActive.isEmpty()){
            boolean hasPrimary = avatarActive.stream()
                    .anyMatch(Image::isPrimary);
            if(!hasPrimary){
                avatarActive.forEach(img -> img.setPrimary(false));
                avatarActive.get(0).setPrimary(true);
                imageRepo.saveAll(avatarActive);
            }
        }
        //list anh moi
        List<Image> avatar = new ArrayList<>();
        List<String> savedFilePaths = new ArrayList<>();

        try{
            //luu anh moi
            if(images != null && !images.isEmpty()){
                for(MultipartFile file : images) {
                    ImageDTO imageDTO = fileStorageService.save(file, ObjectType.STUDENT, ImageType.IMAGE);
                    Image image = new Image();
                    image.setUrl(imageDTO.getUrl());
                    image.setFileName(imageDTO.getFilename());
                    image.setType(ImageType.IMAGE);
                    image.setObjectType(ObjectType.STUDENT);
                    image.setObjectId(student.getId());
                    avatar.add(image);
                    savedFilePaths.add(imageDTO.getUrl());
                }
            }
            //luu lai avatar
            student.getAvatar().addAll(avatar);
            imageRepo.saveAll(avatar);
            studentRepo.save(student);
            return studentMap.toStdResponse(student);

        }
        catch (Exception e){
            fileStorageService.deleteFiles(savedFilePaths);
            throw e;
        }
    }

    @Transactional
    public void deleteStudent(Long id){
        Student student = studentRepo.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENTNOTFOUND));
        student.setStatus(Status.DELETED);
        studentRepo.save(student);
    }

    public void export(HttpServletResponse response, StudentQuery query) {
        Long count = studentRepo.count(query);
        if (count == 0) {
            throw  new AppException(ErrorCode.NO_DATA_TO_EXPORT);
        }
        List<StudentDTO> lists = studentRepo.search(query, null);

        try (
                InputStream templateStream = new ClassPathResource("templates/student_template.xlsx").getInputStream();
                OutputStream outputStream = response.getOutputStream()
        ) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = LocalDateTime.now().format(formatter);
            String resultFileName = String.format("student_report_%s.xlsx", timestamp);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + resultFileName + "\"");

            Context context = new Context();
            context.putVar("lists", lists);

            JxlsHelper.getInstance().processTemplate(templateStream, outputStream, context);

            response.flushBuffer();
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
    }
}
