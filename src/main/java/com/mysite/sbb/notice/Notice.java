package com.mysite.sbb.notice;

import java.time.LocalDateTime;

import com.mysite.sbb.user.SiteUser;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Notice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(length = 200)
	private String subject;
	
	@Column(columnDefinition = "TEXT")
	private String content;
	
	private LocalDateTime createDate;
	
	@ManyToOne
	private SiteUser author;
	
	@Column(columnDefinition = "integer default 0")
	private Integer viewCount = 0;
}
