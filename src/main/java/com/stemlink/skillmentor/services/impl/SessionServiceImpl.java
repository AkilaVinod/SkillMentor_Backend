package com.stemlink.skillmentor.services.impl;

import com.stemlink.skillmentor.Repositories.MentorRepository;
import com.stemlink.skillmentor.Repositories.SessionRepository;
import com.stemlink.skillmentor.Repositories.StudentRepository;
import com.stemlink.skillmentor.Repositories.SubjectRepository;
import com.stemlink.skillmentor.dto.request.SessionRequestDTO;
import com.stemlink.skillmentor.entities.Mentor;
import com.stemlink.skillmentor.entities.Session;
import com.stemlink.skillmentor.entities.Student;
import com.stemlink.skillmentor.entities.Subject;
import com.stemlink.skillmentor.exceptions.SkillMentorException;
import com.stemlink.skillmentor.services.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final SubjectRepository subjectRepository;
    private final ModelMapper modelMapper;

    public Session createNewSession(SessionRequestDTO sessionRequestDTO){
        try{
            Student student = studentRepository.findById(sessionRequestDTO.getStudentId()).orElseThrow(
                    () -> new SkillMentorException("Student not found", HttpStatus.NOT_FOUND)
            );
            Mentor mentor = mentorRepository.findByMentorId(String.valueOf(sessionRequestDTO.getMentorId())).orElseThrow(
                    () -> new SkillMentorException("Mentor not found", HttpStatus.NOT_FOUND)
            );
            Subject subject = subjectRepository.findById(sessionRequestDTO.getSubjectId()).orElseThrow(
                    () -> new SkillMentorException("Subject not found", HttpStatus.NOT_FOUND)
            );

            Session session = modelMapper.map(sessionRequestDTO , Session.class);
            session.setStudent(student);
            session.setMentor(mentor);
            session.setSubject(subject);

            return sessionRepository.save(session);
        } catch (SkillMentorException skillMentorException) {
            log.error("Dependencies not found to map: {}, Failed to create new session", skillMentorException.getMessage());
            throw skillMentorException;
        } catch (Exception exception) {
            log.error("Failed to create session", exception);
            throw new SkillMentorException("Failed to create new session", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Session> getAllSessions() {
        return sessionRepository.findAll(); // SELECT * FROM sessions
    }

    public Session getSessionById(Long id) {
        return sessionRepository.findById(id).orElseThrow(
                () -> new SkillMentorException("Session not found",HttpStatus.NOT_FOUND)
        );
    }

    public Session updateSessionById(Long id, SessionRequestDTO updatedSessionDTO){
        Session session = sessionRepository.findById(id).orElseThrow(
                () -> new SkillMentorException("Session not found",HttpStatus.NOT_FOUND)
        );

        // source -> destination
        modelMapper.map(updatedSessionDTO, session);

        // Update the related entities
        if (updatedSessionDTO.getStudentId() != null) {
            Student student = studentRepository.findById(updatedSessionDTO.getStudentId()).get();
            session.setStudent(student);
        }
        if (updatedSessionDTO.getMentorId() != null) {
            Mentor mentor = mentorRepository.findByMentorId(String.valueOf(updatedSessionDTO.getMentorId()))
                    .orElseThrow(() -> new SkillMentorException("Mentor not found", HttpStatus.NOT_FOUND));
            session.setMentor(mentor);
        }
        if (updatedSessionDTO.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(updatedSessionDTO.getSubjectId()).get();
            session.setSubject(subject);
        }
        return sessionRepository.save(session);
    }

    public void deleteSession(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() ->
                        new SkillMentorException("Session not found", HttpStatus.NOT_FOUND)
                );
        sessionRepository.delete(session);
    }

    public List<Session> getSessionsByStudentEmail(String email){
        return sessionRepository.findByStudent_Email(email);
    }

}
