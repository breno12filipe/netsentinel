package com.netsentinel.repository;

import com.netsentinel.model.Target;
import com.netsentinel.model.TargetType;
import java.util.List;
import java.util.Optional;

public interface TargetRepository {
    Target save(Target target);
    Optional<Target> findById(String id);
    List<Target> findAll();
    List<Target> findByEnabledAndType(Boolean enabled, TargetType type);
    boolean deleteById(String id);
    boolean existsById(String id);
}
