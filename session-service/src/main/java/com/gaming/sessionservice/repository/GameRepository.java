package com.gaming.sessionservice.repository;

import com.gaming.sessionservice.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {}