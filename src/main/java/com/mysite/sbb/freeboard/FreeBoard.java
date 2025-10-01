package com.mysite.sbb.freeboard;

import java.time.LocalDateTime;
import java.util.List;
import com.mysite.sbb.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class FreeBoard {

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
	
	@OneToMany(mappedBy = "freeBoard", cascade = CascadeType.REMOVE)
	private List<FreeComment> commentList;
	
	private String imageUrl;
	
	private LocalDateTime modifyDate;
	
	@Column(columnDefinition = "integer default 0")
	private Integer viewCount = 0;
}
