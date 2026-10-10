package com.netsentinel.repository;

import com.netsentinel.model.Target;
import com.netsentinel.model.TargetType;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Utilizamos o `ConcurrentHashMap`, que nos garante thread-safety com
 * travamento por segmento sem bloquear leituras concorrentes, já que vamos
 * usar um serviço de scheduling que lê os alvos em segundo plano enquanto
 * a API estará cadastrando ou editando alvos simultaneamento.
 */
@Repository
public class InMemoryTargetRepository implements TargetRepository {

    // Estrutura thread-safe indexada pelo ID do Target
    private final Map<String, Target> storage = new ConcurrentHashMap<>();

    @Override
    public Target save(Target target) {
        if (target.getId() == null || target.getId().isBlank()) {
            // Gera um ID único se for um novo cadastro
            target.setId("tgt-" + UUID.randomUUID().toString().substring(0, 8));
        }
        storage.put(target.getId(), target);
        return target;
    }

    @Override
    public Optional<Target> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Target> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Target> findByEnabledAndType(Boolean enabled, TargetType type) {
        return storage.values().stream()
                .filter(target -> enabled == null || target.isEnabled() == enabled)
                .filter(target -> type == null || target.getType() == type)
                .toList();
    }

    @Override
    public boolean deleteById(String id) {
        return storage.remove(id) != null;
    }

    @Override
    public boolean existsById(String id) {
        return storage.containsKey(id);
    }
}
