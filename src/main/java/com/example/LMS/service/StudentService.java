package com.example.LMS.service;

import com.example.LMS.Exception.AppException;
import com.example.LMS.Exception.ErrorCode;
import com.example.LMS.dto.Request.StudentRequest;
import com.example.LMS.dto.Request.StudentUpdate;
import com.example.LMS.dto.Response.StudentResponse;
import com.example.LMS.entity.Image;
import com.example.LMS.entity.Student;
import com.example.LMS.enums.ImageType;
import com.example.LMS.enums.ObjectType;
import com.example.LMS.enums.Status;
import com.example.LMS.mapper.StudentMap;
import com.example.LMS.repo.ImageRepo;
import com.example.LMS.repo.StudentRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

        List<Image> avatar = new ArrayList<>();
        List<String> savedFilePaths = new ArrayList<>();

        try{
            if(images != null && !images.isEmpty()){
                boolean first = true;
                for(MultipartFile file : images) {
                    String url = fileStorageService.save(file, ObjectType.STUDENT, ImageType.IMAGE);
                    Image image = new Image();
                    image.setUrl(url);
                    image.setType(ImageType.IMAGE);
                    image.setObjectType(ObjectType.STUDENT);
                    image.setObjectId(student.getId());
                    image.setPrimary(first);
                    first = false;
                    avatar.add(image);
                    savedFilePaths.add(url);
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

    public Page<StudentResponse> searchStudent(int page, int size, String name, String email){
        if (name != null && !name.isBlank()) {
            name = "%" + name.replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_")
                    .toLowerCase() + "%";
        } else {
            name = null;
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Long> studentIds = studentRepo.searchIds(pageable, name, email);
        if (studentIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<Long> ids = studentIds.getContent();
        List<Student> students = studentRepo.findAllByImageAndEnrollments(ids);

        Map<Long, Student> studentMapId = students.stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        List<Student> studentResponse = ids.stream()
                .map(studentMapId::get)
                .toList();
        List<StudentResponse> studentResponseList = studentMap.toStdResponses(studentResponse);
        return new PageImpl<>(studentResponseList, pageable, studentIds.getTotalElements());
    }

    @Transactional(rollbackFor = Exception.class)
    public StudentResponse updateStudent(Long id, StudentUpdate studentUpdate, List<MultipartFile> images,
                                         List<Long> deleteAvatarsId, Long mainAvatarId) throws IOException {
        Student student = studentRepo.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENTNOTFOUND));

        studentMap.updateStudent(studentUpdate, student);

        if(deleteAvatarsId != null && !deleteAvatarsId.isEmpty()){
            for(Long deleteAvatar : deleteAvatarsId){
                Image image = imageRepo.findById(deleteAvatar).orElseThrow(() -> new AppException(ErrorCode.IMAGENOTFOUND));
                String url = image.getUrl();
                List<String> urls = new ArrayList<>();
                urls.add(url);
                fileStorageService.deleteFiles(urls);
                imageRepo.deleteById(deleteAvatar);
                student.getAvatar().removeIf(img -> deleteAvatarsId.contains(img.getId()));
            }
        }
        boolean hasPrimary = student.getAvatar().stream().anyMatch(Image::isPrimary);
        if(!hasPrimary && !student.getAvatar().isEmpty()){
            student.getAvatar().forEach(img -> img.setPrimary(false));
            student.getAvatar().get(0).setPrimary(true);
        }
        List<Image> avatar = new ArrayList<>();
        List<String> savedFilePaths = new ArrayList<>();

        try{
            if(images != null && !images.isEmpty()){
                for(MultipartFile file : images) {
                    String path = fileStorageService.save(file, ObjectType.STUDENT, ImageType.IMAGE);
                    Image image = new Image();
                    image.setUrl(path);
                    image.setType(ImageType.IMAGE);
                    image.setObjectType(ObjectType.STUDENT);
                    image.setObjectId(student.getId());
                    avatar.add(image);
                    savedFilePaths.add(path);
                }
            }
            imageRepo.saveAll(avatar);
            student.setAvatar(avatar);
            studentRepo.save(student);

            if(mainAvatarId != null){
                for(Image img : student.getAvatar()){
                    img.setPrimary(img.getId().equals(mainAvatarId));
                }
            } else if(!student.getAvatar().isEmpty()){
                student.getAvatar().forEach(img -> img.setPrimary(false));
                student.getAvatar().get(0).setPrimary(true);
            }

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
}
