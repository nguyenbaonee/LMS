package com.example.LMS.service;

import com.example.LMS.Exception.AppException;
import com.example.LMS.Exception.ErrorCode;
import com.example.LMS.dto.Request.StudentQuery;
import com.example.LMS.dto.Request.StudentRequest;
import com.example.LMS.dto.Request.StudentUpdate;
import com.example.LMS.dto.Response.StudentResponse;
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
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepo studentRepo;
    private final StudentMap studentMap;
    private final FileStorageService fileStorageService;
    private final ImageRepo imageRepo;

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

    public Page<StudentDTO> searchStudent(StudentQuery query){
        Pageable pageable = PageRequest.of(query.getPage(), query.getSize());
        Long count = studentRepo.count(query);
        if (count == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<StudentDTO> students = studentRepo.search(query, pageable);

        List<Long> ids = students.stream().map(StudentDTO::getId).collect(Collectors.toList());
        List<StudentAvatarDTO> studentDTOS = studentRepo.findStudentAvatars(ids);
        Map<Long, List<Image>> studentMapId = studentDTOS.stream()
                .collect(Collectors.groupingBy(
                        StudentAvatarDTO::getId,
                        Collectors.mapping(StudentAvatarDTO::getImage, Collectors.toList())
                ));
        students.forEach(student -> student.setAvatar(studentMapId.getOrDefault(student.getId(), List.of())));
        return new PageImpl<>(students, pageable, count);
    }

    @Transactional(rollbackFor = Exception.class)
    public StudentResponse updateStudent(Long id, StudentUpdate studentUpdate, List<MultipartFile> images,
                                         List<Long> deleteAvatarsId, Long mainAvatarId) throws IOException {
        //check theo id va Status
        Student student = studentRepo.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENTNOTFOUND));
        if(studentRepo.existsByEmail(studentUpdate.getEmail()) && !studentUpdate.getEmail().equals(student.getEmail())){
            throw new AppException(ErrorCode.EMAILEXISTS);
        }
        studentMap.updateStudent(studentUpdate, student);

        //Xoa mem cac anh co trong list xoa
        if(deleteAvatarsId != null && !deleteAvatarsId.isEmpty()) {
//            List<Image> imagesDelete = imageRepo.findAllById(deleteAvatarsId);
            List<Image> imagesDelete = imageRepo.findAllByIdInAndStatus(deleteAvatarsId,Status.ACTIVE);
            if(imagesDelete.size() != deleteAvatarsId.size()){
                throw new AppException(ErrorCode.AVATAR_NOT_FOUND);
            }
            imagesDelete.forEach(image -> {image.setStatus(Status.DELETED);});
            imageRepo.saveAll(imagesDelete);
        }

        //list anh moi
        List<Image> avatar = new ArrayList<>();
        List<String> savedFilePaths = new ArrayList<>();

        try{
            //luu anh moi
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
            //luu lai avatar
            student.getAvatar().addAll(avatar);
            imageRepo.saveAll(avatar);
            studentRepo.save(student);

            List<Image> avatarActive = student.getAvatar().stream()
                    .filter(img -> img.getStatus() == Status.ACTIVE)
                    .toList();
            //xu ly avatar hien thi
            if(mainAvatarId != null){
                boolean exists = avatarActive.stream()
                        .anyMatch(img -> img.getId().equals(mainAvatarId));

                if (!exists) {
                    throw new AppException(ErrorCode.AVATAR_NOT_FOUND);
                }
                for(Image img : avatarActive) {
                    img.setPrimary(img.getId().equals(mainAvatarId));
                }
            } else if(!avatarActive.isEmpty()){
                boolean hasPrimary = avatarActive.stream()
                        .anyMatch(Image::isPrimary);
                if(!hasPrimary){
                    avatarActive.forEach(img -> img.setPrimary(false));
                    avatarActive.get(0).setPrimary(true);
                }
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
