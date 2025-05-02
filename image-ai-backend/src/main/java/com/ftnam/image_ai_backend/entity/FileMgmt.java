package com.ftnam.image_ai_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "image_mgmt")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileMgmt {
    @Id
    @Column(name = "file_id")
    String id;

    @Column(name = "owner_id")
    String ownerId;

    @Column(name = "content_type")
    String contentType;

    long size;

    @Column(name = "md5_checksum")
    String md5Checksum;

    String path;
}
