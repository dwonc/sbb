package com.mysite.sbb.freeboard;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mysite.sbb.CloudinaryService;
import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.notice.Notice;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class FreeBoardService {
    
    private final FreeBoardRepository freeBoardRepository;
	private final CloudinaryService cloudinaryService;
	
    public Page<FreeBoard> getList(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createDate"));
        return this.freeBoardRepository.findAll(pageable);
    }
    
    public FreeBoard getFreeBoard(Integer id) {
        Optional<FreeBoard> freeBoard = this.freeBoardRepository.findById(id);
        if (freeBoard.isPresent()) {
            return freeBoard.get();
        } else {
            throw new DataNotFoundException("freeboard not found");
        }
    }
    
    public void create(String subject, String content, SiteUser user, MultipartFile imageFile) throws IOException {
        FreeBoard freeBoard = new FreeBoard();
        freeBoard.setSubject(subject);
        freeBoard.setContent(content);
        freeBoard.setCreateDate(LocalDateTime.now());
        freeBoard.setAuthor(user);
        
        if (imageFile != null && !imageFile.isEmpty()) {
			String imageUrl = cloudinaryService.uploadImage(imageFile);
			freeBoard.setImageUrl(imageUrl);
		}

        this.freeBoardRepository.save(freeBoard);
    }
    
    public void modify(FreeBoard freeboard, String subject, String content, MultipartFile imageFile) throws IOException {
    	freeboard.setSubject(subject);
    	freeboard.setContent(content);
    	freeboard.setModifyDate(LocalDateTime.now());
	    
	    // 새 이미지가 업로드되면 처리
	    if (imageFile != null && !imageFile.isEmpty()) {
	        // 기존 이미지가 있으면 삭제
	        if (freeboard.getImageUrl() != null) {
	            cloudinaryService.deleteImage(freeboard.getImageUrl());
	        }
	        // 새 이미지 업로드
	        String imageUrl = cloudinaryService.uploadImage(imageFile);
	        freeboard.setImageUrl(imageUrl);
	    }
	    
	    this.freeBoardRepository.save(freeboard);
	}
    
    public void delete(FreeBoard freeboard) {
		this.freeBoardRepository.delete(freeboard);
	}
    
    public void increaseViewCount(FreeBoard freeboard) {
		if (freeboard.getViewCount() == null) {
			freeboard.setViewCount(0);
		}
		freeboard.setViewCount(freeboard.getViewCount() + 1);
		this.freeBoardRepository.saveAndFlush(freeboard);
	}
    
    public List<FreeBoard> getFreeboardByUser(SiteUser user) {
	    return freeBoardRepository.findByAuthorOrderByCreateDateDesc(user);
	}
    
    public List<FreeBoard> getRecentFreeBoards(int limit) {
	    Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createDate"));
	    return freeBoardRepository.findAll(pageable).getContent();
	}
}