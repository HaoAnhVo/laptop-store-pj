package com.codegym.salesmanagement.model.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserProfileDTO {
    @NotBlank(message = "Vui lòng điền vào trường này")
    @Email(message = "Sai định dạng email")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Vui lòng điền vào trường này")
    @Size(max = 255, message = "Họ tên không được vượt quá 255 ký tự")
    @Column(name = "full_name", nullable = false)
    private String fullname;

    @NotBlank(message = "Vui lòng điền vào trường này")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Sai định dạng số điện thoại")
    @Column(name = "phone", unique = true, nullable = false)
    private String phone;

    @NotBlank(message = "Vui lòng điền vào trường này")
    @Column(name = "address", nullable = false)
    private String address;

    private MultipartFile avatar;
    private String avatarPath;
}
