package com.postify.main.repository;

import com.postify.main.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
@Repository
public interface  TagRepository extends JpaRepository<Tag, Integer> {
   Optional<Tag> findByName(String name);

   @Transactional
   void deleteByName(String name);
}
