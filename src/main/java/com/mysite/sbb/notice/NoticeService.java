package com.mysite.sbb.notice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.freeboard.FreeBoard;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NoticeService {

	private final NoticeRepository noticeRepository;

	public Page<Notice> getList(int page) {
		Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createDate"));
		return this.noticeRepository.findAll(pageable);
	}

	public void create(String subject, String content, SiteUser user) {
		Notice notice = new Notice();
		notice.setSubject(subject);
		notice.setContent(content);
		notice.setCreateDate(LocalDateTime.now());
		notice.setAuthor(user);
		this.noticeRepository.save(notice);
	}

	public Notice getNotice(Integer id) {
		Optional<Notice> notice = this.noticeRepository.findById(id);
		if (notice.isPresent()) {
			return notice.get();
		} else {
			throw new DataNotFoundException("notice not found");
		}
	}

	public void increaseViewCount(Notice notice) {
		if (notice.getViewCount() == null) {
			notice.setViewCount(0);
		}
		notice.setViewCount(notice.getViewCount() + 1);
		this.noticeRepository.saveAndFlush(notice);
	}
	
	public List<Notice> getRecentNotices(int limit) {
	    Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createDate"));
	    return noticeRepository.findAll(pageable).getContent();
	}

}
