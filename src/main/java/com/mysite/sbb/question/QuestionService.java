package com.mysite.sbb.question;

import com.mysite.sbb.CloudinaryService;
import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.answer.Answer;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class QuestionService {

	private final QuestionRepository questionRepository;
	private final CloudinaryService cloudinaryService;

	private Specification<Question> search(String kw) {
		return new Specification<>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
				query.distinct(true);
				Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
				Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
				Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
				return cb.or(cb.like(q.get("subject"), "%" + kw + "%"), // 제목
						cb.like(q.get("content"), "%" + kw + "%"), // 내용
						cb.like(u1.get("username"), "%" + kw + "%"), cb.like(a.get("content"), "%" + kw + "%"),
						cb.like(u2.get("username"), "%" + kw + "%"));
			}
		};
	}

	public List<Question> getList() {
		return this.questionRepository.findAll();
	}

	public Question getQuestion(Integer id) {
		Optional<Question> question = this.questionRepository.findById(id);

		if (question.isPresent()) {
			return question.get();
		} else {
			throw new DataNotFoundException("question not found");
		}
	}

	public void create(String subject, String content, SiteUser user, MultipartFile imageFile) throws IOException {
		Question q = new Question();
		q.setSubject(subject);
		q.setContent(content);
		q.setCreateDate(LocalDateTime.now());
		q.setAuthor(user);

		if (imageFile != null && !imageFile.isEmpty()) {
			String imageUrl = cloudinaryService.uploadImage(imageFile);
			q.setImageUrl(imageUrl);
		}

		this.questionRepository.save(q);
	}

	public Page<Question> getList(int page, String kw) {
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate"));
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));

		Specification<Question> spec = search(kw);
		return this.questionRepository.findAll(spec, pageable);
	}

	public void modify(Question question, String subject, String content, MultipartFile imageFile) throws IOException {
	    question.setSubject(subject);
	    question.setContent(content);
	    question.setModifyDate(LocalDateTime.now());
	    
	    // 새 이미지가 업로드되면 처리
	    if (imageFile != null && !imageFile.isEmpty()) {
	        // 기존 이미지가 있으면 삭제
	        if (question.getImageUrl() != null) {
	            cloudinaryService.deleteImage(question.getImageUrl());
	        }
	        // 새 이미지 업로드
	        String imageUrl = cloudinaryService.uploadImage(imageFile);
	        question.setImageUrl(imageUrl);
	    }
	    
	    this.questionRepository.save(question);
	}

	public void delete(Question question) {
		this.questionRepository.delete(question);
	}

	public void vote(Question question, SiteUser siteUser) {
		question.getVoter().add(siteUser);
		this.questionRepository.save(question);
	}
	
	public void increaseViewCount(Question question) {
		question.setViewCount(question.getViewCount() + 1);
		this.questionRepository.saveAndFlush(question);
	}

	public List<Question> getQuestionsByUser(SiteUser user) {
	    return questionRepository.findByAuthorOrderByCreateDateDesc(user);
	}
	
	public List<Question> getPopularQuestions(int limit) {
	    Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "viewCount"));
	    Page<Question> page = questionRepository.findAll(Specification.where(null), pageable);
	    return page.getContent();
	}
}
