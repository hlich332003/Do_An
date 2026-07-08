package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.LichSuChatAi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LichSuChatAiRepository extends JpaRepository<LichSuChatAi, Long> {}
