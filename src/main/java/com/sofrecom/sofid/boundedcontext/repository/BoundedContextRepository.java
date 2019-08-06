package com.sofrecom.sofid.boundedcontext.repository;

import com.sofrecom.sofid.boundedcontext.domain.BoundedContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 *
 */
@Repository
public interface BoundedContextRepository extends JpaRepository<BoundedContext, Integer> {


}